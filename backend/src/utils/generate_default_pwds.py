import encryption
from utils.encryption import AESCipher

def create_encrypted_pwds(pwds):
    aes_cipher = AESCipher.keyFromVariable(encryption.key)
    for pwd in pwds:
        print aes_cipher.encrypt(pwd)

def create_decrypted_pwds(pwds):
    aes_cipher = AESCipher.keyFromVariable(encryption.key)
    for pwd in pwds:
        print aes_cipher.decrypt(pwd)
    
print "encrypted pwds"
pwds = ["", "iotcm2016", "iotcm2017"]
create_encrypted_pwds(pwds)

print "-------------------"

print "decrypted pwds"
pwds = ["RgnvhztbfhBUfeXTWG0xI15ZK3uHm3lBdwzZW23BI6Y=", "SarYfRIp7NDsodQC8J5JrgOjJK+f7W8PuOnGqDbiSi0="]
create_decrypted_pwds(pwds)
