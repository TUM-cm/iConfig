import base64
import hashlib
import random

from Crypto.Cipher import AES
from Crypto import Random

# https://www.dlitz.net/software/pycrypto/api/current/Crypto.Cipher.AES-module.html
# http://stackoverflow.com/questions/12524994/encrypt-decrypt-using-pycrypto-aes-256

BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS) 
unpad = lambda s : s[0:-ord(s[-1])]
key = "TVVgoAJNLa2sWFXeDOoSP/0GKQSER7bfTJ4RfTdxYqo="

class AESCipher(object):
    
    def __init__(self, key):
        self.key = key
        self.mode = AES.MODE_CBC
    
    @classmethod
    def keyFromFile(cls, keypath):        
        key = AESCipher.load_key(keypath)
        return cls(key)
    
    @classmethod
    def keyFromGen(cls, pwd_len=32):
        password = AESCipher.generate_password(pwd_len)
        key = hashlib.sha256(password).digest()
        return cls(key)
    
    @classmethod
    def keyFromVariable(cls, key):
        return cls(base64.b64decode(key))
    
    @classmethod
    def keyFromPassword(cls, password):
        key = hashlib.sha256(password.encode()).digest()
        return cls(key)
    
    def encrypt(self, data):
        if data and len(data) > 0:
            message = pad(data)
            iv = Random.new().read(AES.block_size)
            cipher = AES.new(self.key, AES.MODE_CBC, iv)
            return base64.b64encode(iv + cipher.encrypt(message)).decode('utf-8')
        else:
            return data
    
    def decrypt(self, data):
        if data and len(data) > 0:        
            enc = base64.b64decode(data)
            iv = enc[:AES.block_size]
            cipher = AES.new(self.key, AES.MODE_CBC, iv)
            return unpad(cipher.decrypt(enc[AES.block_size:])).decode('utf-8')
        else:
            return data
    
    def save_key(self, filepath):
        f = open(filepath, "wb")
        f.write(self.key)
        f.close()
    
    @staticmethod
    def generate_password(pwd_len):
        password = ''
        for _ in range(int(pwd_len)):
            password += chr(random.randint(33,126))
        return password
    
    @staticmethod 
    def load_key(keypath):
        f = open(keypath, "rb")
        data = f.read()
        f.close()
        return data
    
    @staticmethod
    def load_password(filepath):
        f = open(filepath, "r")
        data = f.read()
        f.close()
        return data
    
    @staticmethod
    def create_key(password):
        return hashlib.sha256(password).digest()
