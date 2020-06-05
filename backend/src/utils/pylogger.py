import logging
import os
from logging.handlers import TimedRotatingFileHandler

class MyLogger:
	
	def __init__(self):
		logging.getLogger('default')
		basepath = os.path.dirname(__file__)
		f = os.path.abspath(os.path.join(basepath, "..", "beacon_config.log"))
		logging.basicConfig(format='%(asctime)-15s %(levelname)s %(message)s', datefmt="%m/%d/%Y %I:%M:%S %p", filename=f, level="INFO")
		self.logger = logging.getLogger("beaconconfig")
		self.logger.info('Created an instance of Beacon config logger')		
		handler = TimedRotatingFileHandler(f, when="d", interval=1, backupCount=7)
		self.logger.addHandler(handler)
	
	def info(self, string):
		logging.info(string)
			
	def error(self, string):
		logging.error(string)
			
	def debug(self, string):
		logging.debug(string)