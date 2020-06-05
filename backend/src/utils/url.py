import requests

class Url:
    
    def __init__(self, service, key):        
        self.service = service
        self.key = key
        self.url_scheme_prefix = ["http://www.", "https://www.", "http://", "https://"]
        self.shorten_url_domain = [".gl", ".ly"]
        self.max_length_url = 17
    
    #POST https://www.googleapis.com/urlshortener/v1/url
    #Content-Type: application/json
    #{"longUrl": "http://www.google.com/"}
    def shorten(self, url):
        url_service = self.service + '?key=' + self.key
        payload = {'longUrl': url}
        headers = {'content-type': 'application/json'}
        request = requests.post(url_service, json=payload, headers=headers)
        response = request.json()
        return response['id']
    
    # GET https://www.googleapis.com/urlshortener/v1/url?shortUrl=http://goo.gl/fbsS
    def expand(self, url):
        if (self.url_shortened(url)):        
            payload = {'key': self.key, 'shortUrl': url}
            request = requests.get(self.service, params=payload)
            response = request.json()
            return response['longUrl']
        else:
            return url
    
    def url_shortened(self, url):
        for domain in self.shorten_url_domain:
            if domain in url:
                return True
        return False
    
    def check(self, url):
        for url_prefix in self.url_scheme_prefix:
            if url.startswith(url_prefix):
                url = url[len(url_prefix):]
                break        
        if len(url) > self.max_length_url:
            return True
        else:
            return False