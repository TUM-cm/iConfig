import os
import cherrypy
import sys

from utils.config import Config
from utils.pylogger import MyLogger
from db.MongoDBConnector import MongoDBConnector
from utils.input import Input

# Web service
from jinja2 import Environment, FileSystemLoader
from web.BeaconWebApplication import BeaconWebApplication
from db.DbBeaconWebApplication import DbBeaconWebApplication

# API
from backend.ConfigBeacon import ConfigBeacon
from backend.ListBeacons import ListBeacons
from backend.ImageBeacon import ImageBeacon
from backend.DataBeacon import DataBeacon
from db.DbBeaconAPI import DbBeaconAPI

basepath = os.path.dirname(__file__)
template_path = os.path.abspath(os.path.join(basepath, "web", "templates"))
env = Environment(loader=FileSystemLoader(template_path))

def error_page_404(status, message, traceback, version):
    template = env.get_template('error.html')
    content = list("<p>")
    content.append(message)
    content.append("</p>")
    return template.render(title='404 Error', button_text="Go to overview", content="".join(content))

def error_page_401(status, message, traceback, version):
    template = env.get_template('error.html')    
    content = list("<p>")
    content.append(message)
    content.append("</p>")
    return template.render(title='401 Error', button_text="Login", content="".join(content))

# Return HTML error message when an exception is thrown in the code. 
# This to avoid showing a stack trace with sensitive information.
def handle_error():
    cherrypy.response.status = 500
    cherrypy.response.body = [
        "<html><body>Sorry, an error occured.</body></html>".encode()
    ]

class Webserver(object):
    
    def __init__(self):
        self.logger = MyLogger()
        self.webserver = Config("Webserver")
        self.web_application = Config("WebApplication")
        self.restful_api = Config("BeaconRestfulApi")
        mongo_db_connector = MongoDBConnector()
        if Input.config == "demo":
            server_config = {
                'error_page.404': error_page_404,
                'error_page.401': error_page_401,
                'engine.autoreload.on': False,
                'log.access_file': os.path.abspath(os.path.join(basepath, "access.log")),
                'log.error_file': os.path.abspath(os.path.join(basepath, "error.log"))
            }
        else:
            userpassdict = { self.webserver.get_value("username"): self.webserver.get_value("password")}
            check_password = cherrypy.lib.auth_basic.checkpassword_dict(userpassdict)
            server_config = {
                'error_page.404': error_page_404,
                'error_page.401': error_page_401,
                'engine.autoreload.on': False,                
                # Basic Auth
                'tools.auth_basic.on': True,
                'tools.auth_basic.realm': self.webserver.get_value("realm"),
                'tools.auth_basic.checkpassword': check_password,            
                # SSL
                'server.ssl_module':'pyopenssl',
                'server.ssl_certificate': self.webserver.get_value("certificate"),
                'server.ssl_private_key': self.webserver.get_value("key"),            
                'log.access_file': os.path.abspath(os.path.join(basepath, "access.log")),
                'log.error_file': os.path.abspath(os.path.join(basepath, "error.log"))
            }
            cherrypy.log.screen = None
            server_config['request.error_response'] = handle_error
        
        cherrypy.config.update(server_config)
        
        db_beacon_web_application = DbBeaconWebApplication(mongo_db_connector)
        db_beacon_api = DbBeaconAPI(mongo_db_connector)
        image_path = db_beacon_web_application.get_field("image path")
        self.create_web_application(db_beacon_web_application, db_beacon_api, image_path)
        self.create_api(db_beacon_api, image_path)
    
    def create_web_application(self, db_beacon_web_application, db_beacon_api, image_path):
        app_config = {
            '/': {
                'tools.staticdir.root': os.getcwd()
            },
            '/images': {
                'tools.staticdir.on': True,
                'tools.staticdir.dir': image_path
            }
        }
        cherrypy.tree.mount(BeaconWebApplication(db_beacon_web_application, db_beacon_api, env),
                            self.web_application.get_value("servicepath"), config=app_config)
    
    def create_api(self, db_beacon_api, image_path):
        api_config = {
            '/': {
                'request.dispatch': cherrypy.dispatch.MethodDispatcher()
            }
        }
        cherrypy.tree.mount(ConfigBeacon(db_beacon_api), self.restful_api.get_value("serviceconfig"), config=api_config)
        cherrypy.tree.mount(ListBeacons(db_beacon_api), self.restful_api.get_value("servicelist"), config=api_config)
        cherrypy.tree.mount(ImageBeacon(db_beacon_api, image_path), self.restful_api.get_value("serviceimage"), config=api_config)
        cherrypy.tree.mount(DataBeacon(db_beacon_api), self.restful_api.get_value("servicedata"), config=api_config)
    
    def start(self):
        cherrypy.config.update({'server.socket_host': self.webserver.get_value("address"), })
        cherrypy.config.update({'server.socket_port': int(self.webserver.get_value("port")), })
        cherrypy.engine.start()
        # Listen for requests
        cherrypy.engine.block()
    
    def stop(self):
        cherrypy.engine.stop()

def main():
    Input(sys.argv)
    webserver = Webserver()
    webserver.start()
    
if __name__ == "__main__":
    main()