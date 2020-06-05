from hashlib import md5

def generate_password(username, realm, password):
    md5_hex = lambda s: md5(s).hexdigest()
    return md5_hex('%s:%s:%s' % (username, realm, password))

print generate_password("beacon_config", "Beacon Webservice", "cm2016beacons")

# alice:wonderland:3238cdfe91a8b2ed8e39646921a02d4c
#print generate_password("alice", "wonderland", "4x5istwelve")