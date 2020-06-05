import ConfigParser
import os        
from utils.input import Input

class Config(object):
    
    def __init__(self, section_name):
        basepath = os.path.dirname(__file__)
        if Input.config == "demo":
            f = os.path.abspath(os.path.join(basepath, "..", "config_demo.ini"))
        else:
            f = os.path.abspath(os.path.join(basepath, "..", "config.ini"))
        self.config = ConfigParser.ConfigParser()
        self.config.read(f)
        self.section = self.config_section_map(section_name)
    
    def config_section_map(self, section):
        dict1 = {}    
        options = self.config.options(section)
        for option in options:
            try:
                dict1[option] = self.config.get(section, option)
                if dict1[option] == -1:
                    print("skip: %s" % option)
            except:
                print("exception on %s!" % option)
                dict1[option] = None
        return dict1
    
    def get_value(self, key):
        return self.section[key]