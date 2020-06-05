import cherrypy

class ListBeacons():
    
    exposed = True
    
    def __init__(self, db_beacon_api):
        self.db_beacon_api = db_beacon_api
    
    @cherrypy.tools.json_out()
    def GET(self):
        return self.db_beacon_api.get_registered_beacons()