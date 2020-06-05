import cherrypy

class DataBeacon():
    
    exposed = True
    
    def __init__(self, db_beacon_api):
        self.db_beacon_api = db_beacon_api
    
    # Send all beacon configurations for update
    @cherrypy.tools.json_out()
    def GET(self):
        return self.db_beacon_api.get_beacon_configs_to_update()
    
    @cherrypy.tools.json_in()
    @cherrypy.tools.json_out()
    def POST(self):
        beacon = cherrypy.request.json        
        # Update or create beacon
        identifier = beacon[self.db_beacon_api.mongo_db_connector.identifier]
        if self.db_beacon_api.beacon_exists(identifier):
            return self.db_beacon_api.update_beacon(beacon)
        else:
            return self.db_beacon_api.insert_beacon(beacon)