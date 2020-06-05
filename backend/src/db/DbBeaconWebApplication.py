
class DbBeaconWebApplication():
    
    def __init__(self, mongo_db_connector):
        self.mongo_db_connector = mongo_db_connector
    
    def get_all_beacons(self):
        return self.mongo_db_connector.get_collection_beacon_web().find()
    
    def update_beacon(self, beacon, identifier):
        for field in beacon.keys():
            if not field in identifier:
                self.mongo_db_connector.get_collection_beacon_web().update_one(
                    {identifier: beacon[identifier]},
                    {'$set':{field: beacon[field]}},
                    upsert=False)
    
    def get_field(self, field, entry_type="web application"):
        return self.mongo_db_connector.get_field(field, entry_type)
    
    def get_identifier(self):
        return self.mongo_db_connector.identifier