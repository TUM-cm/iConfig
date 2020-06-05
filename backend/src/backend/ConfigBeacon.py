import cherrypy

class ConfigBeacon():
    
    exposed = True
    
    def __init__(self, db_beacon_api):
        self.db_beacon_api = db_beacon_api
    
    @cherrypy.tools.json_out()
    def GET(self, config_type):
        return self.db_beacon_api.get_beacon_config(config_type)