import time
import utils.encryption

from utils.encryption import AESCipher

class DbBeaconAPI():
    
    def __init__(self, mongo_db_connector):
        self.mongo_db_connector = mongo_db_connector
        self.config_comparison_fields = self.get_field("config comparison")
        self.maintenance_field = self.get_field("maintenance field")
        self.timestamp_field = self.get_field("timestamp field")
        self.aes_cipher = AESCipher.keyFromVariable(utils.encryption.key)
    
    def get_beacon_config(self, config_type):
        response = self.mongo_db_connector.get_collection_beacon_template().find_one({"type": config_type},
                                                {self.mongo_db_connector.mongo_db_identifier: False})
        return response
    
    def get_registered_beacons(self):
        db_cursor = self.mongo_db_connector.get_collection_beacon_data().find({},
                            {self.mongo_db_connector.mongo_db_identifier: False,
                            self.mongo_db_connector.identifier: True}).distinct(self.mongo_db_connector.identifier)
        return list(db_cursor)
    
    def beacon_exists(self, identifier):
        count = self.mongo_db_connector.get_collection_beacon_data().find(
                    {self.mongo_db_connector.identifier: identifier},
                    {self.mongo_db_connector.mongo_db_identifier: True}).count()
        if count > 0:
            return True
        else:
            return False
    
    def insert_beacon(self, beacon):        
        self.process_maintenance(beacon)        
        key = self.mongo_db_connector.identifier        
        result_data = self.mongo_db_connector.get_collection_beacon_data().update_one(
            {key : beacon[key]},
            {'$set': beacon},
            upsert=True)
        result_web = self.mongo_db_connector.get_collection_beacon_web().update_one(
            {key : beacon[key]},
            {'$set': beacon},
            upsert=True)
        return all([result_data.acknowledged, result_web.acknowledged])
    
    def update_beacon(self, beacon):
        self.process_maintenance(beacon)
        key = self.mongo_db_connector.identifier
        result = self.mongo_db_connector.get_collection_beacon_data().update_one(
            {key : beacon[key]}, {'$set': beacon}, upsert=True)
        return result.acknowledged
    
    def get_beacon_configs_to_update(self):
        beacons_to_update = []
        beacons_config = self.mongo_db_connector.get_collection_beacon_data().find()
        beacons_web_config = self.mongo_db_connector.get_collection_beacon_web().find({},
                                    {self.mongo_db_connector.mongo_db_identifier: False})
        for beacon_config in beacons_config:
            identifier = beacon_config[self.mongo_db_connector.identifier]
            beacon_web_config = self.get_beacon_web_config(beacons_web_config, identifier)
            if beacon_web_config:
                if self.is_config_updated(beacon_config, beacon_web_config) and \
                        not self.is_broken(beacon_config):
                    beacons_to_update.append(beacon_web_config)
        return beacons_to_update
    
    def get_beacon_web_config(self, beacons_web_config, identifier):
        for beacon_web_config in beacons_web_config:
            if beacon_web_config[self.mongo_db_connector.identifier] == identifier:
                return beacon_web_config
        return None
    
    def is_config_updated(self, beacon_config, beacon_web_config):  
        for config_field in self.config_comparison_fields:
            keys = config_field.split(",")
            if self.config_compare(beacon_config, beacon_web_config, keys):
                return True
        return False
    
    def config_compare(self, beacon_config, beacon_web_config, keys):
        key = keys[0]
        if isinstance(beacon_config[key], dict):
            del keys[0]
            return self.config_compare(beacon_config[key], beacon_web_config[key], keys)
        else:
            if key == "new password":
                dec_config = self.get_cipher().decrypt(beacon_config[key])
                dec_config_web = self.get_cipher().decrypt(beacon_web_config[key])
                return dec_config != dec_config_web
            else:
                return beacon_config[key] != beacon_web_config[key]
    
    def process_maintenance(self, beacon):
        if self.maintenance_field in beacon:
            self.push_to_maintenance(beacon)
            del beacon[self.maintenance_field]
    
    def push_to_maintenance(self, beacon):
        timestamp = self.create_timestamp()
        maintenance = beacon[self.maintenance_field]        
        maintenance[self.timestamp_field] = timestamp
        field = self.maintenance_field        
        key = self.mongo_db_connector.identifier 
        self.mongo_db_connector.get_collection_beacon_web().update_one(
            {key: beacon[key]},
            {'$push': {field: maintenance}},
            upsert=True)
    
    def is_broken(self, beacon):
        # If at least one status is true, then not broken, otherwise broken
        return not any(beacon["status"].values())
    
    def create_timestamp(self):
        return int(time.time())
    
    def get_field(self, field, entry_type="web api"):
        return self.mongo_db_connector.get_field(field, entry_type)
    
    def get_cipher(self):
        return self.aes_cipher
