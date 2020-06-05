import urllib

from pymongo import MongoClient
from utils.config import Config
from utils.input import Input

class MongoDBConnector(object):
    
    def __init__(self):
        self.config = Config(section_name="MongoDB")
        if Input.config == "demo":
            uri = "mongodb://" + self.get_config_value('Address', self.config) + ":" + \
                self.get_config_value('Port', self.config) + "/" + self.get_config_value('Database', self.config)
        else:
            uri = "mongodb://" + self.get_config_value('Username', self.config) + ":" + \
                urllib.quote(self.get_config_value('Password', self.config)) +  "@" + \
                self.get_config_value('Address', self.config) + ":" + \
                self.get_config_value('Port', self.config) + "/" + self.get_config_value('Database', self.config) + \
                    "?authMechanism=" + self.get_config_value('Auth', self.config)
        
        self.mongo_client = MongoClient(uri)
        self.db = self.mongo_client.get_database(self.get_config_value('Database', self.config))
        self.config_collection = Config(section_name="Collections")
        self.collection_beacon_template = self.db.get_collection(self.get_config_value("BeaconTemplateConfig", self.config_collection))
        self.collection_beacon_data = self.db.get_collection(self.get_config_value("BeaconDataConfig", self.config_collection))
        self.collection_beacon_web = self.db.get_collection(self.get_config_value("BeaconWebConfig", self.config_collection))
        self.mongo_db_identifier = "_id"
        self.identifier = self.get_field("identifier", "web application")

    def get_config_value(self, key, config):
        return config.get_value(key.lower())
    
    def get_field(self, field, entry_type):
        response = self.get_collection_beacon_template().find_one(
            {"type": entry_type},
            {self.mongo_db_identifier: False, field: True})[field]
        return response
    
    def get_collection_beacon_template(self):
        return self.collection_beacon_template
    
    def get_collection_beacon_data(self):
        return self.collection_beacon_data
    
    def get_collection_beacon_web(self):
        return self.collection_beacon_web