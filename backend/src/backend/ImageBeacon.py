import cherrypy
import os

class ImageBeacon():
    
    exposed = True    
    
    def __init__(self, db_beacon_api, image_path):        
        self.db_beacon_api = db_beacon_api
        self.image_path = image_path
        self.image_filename_key = self.db_beacon_api.get_field("image filename key")
        self.image_data_key = self.db_beacon_api.get_field("image data key")
        if not os.path.exists(self.image_path):
            os.makedirs(self.image_path)
    
    def create_image(self, image_path, filename, image_data):
        f = open(image_path + filename, "wb")
        f.write(image_data.decode('base64'))
        f.close()
    
    @cherrypy.tools.json_in()
    @cherrypy.tools.json_out()
    def POST(self):
        beacon = cherrypy.request.json
        filename = beacon[self.image_filename_key]
        image_data = beacon[self.image_data_key]
        self.create_image(self.image_path, filename, image_data)
        return True