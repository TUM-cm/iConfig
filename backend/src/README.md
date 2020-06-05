# Setup

## WiFi
Direct WiFi connection between backend (AMD NUC) and frontend (smart glass) via USB WiFi Dongle

## MongoDB
add mongo bin under program to system path
enter mongo in cmd
use edison_testbed
show dbs (not shown until collection via import created)

mongoexport --db edison_testbed --collection BeaconDataConfig --out BeaconDataConfig.json
mongoexport --db edison_testbed --collection BeaconTemplateConfig --out BeaconTemplateConfig.json
mongoexport --db edison_testbed --collection BeaconWebConfig --out BeaconWebConfig.json

mongoimport --db edison_testbed --collection BeaconDataConfig --file BeaconDataConfig.json
mongoimport --db edison_testbed --collection BeaconTemplateConfig --file BeaconTemplateConfig.json
mongoimport --db edison_testbed --collection BeaconWebConfig --file BeaconWebConfig.json

## Software Components
iConfig frontend: git@gitlab.lrz.de:cm/android.git > EddystoneManager
iConfig backend: git@gitlab.lrz.de:cm/beacon_configurator.git > config_webservice

pip install cherrypy
pip install pymongo
Error regarding Win32 dll failed for pycrypto (from Crypto.Cipher import AES)
Install 64 bit version for python 2.7 from http://www.voidspace.org.uk/python/modules.shtml#pycrypto
