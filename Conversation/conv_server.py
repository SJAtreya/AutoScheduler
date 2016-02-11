import tornado.auth
import tornado.escape
import tornado.ioloop
import tornado.web
import os.path
import uuid
import random

from tornado import gen
from tornado.options import define, options, parse_command_line
from tornado.escape import json_encode

import datetime,time


from conversation import parse_sentence

define("port", default=8888, help="run on the given port", type=int)

class BaseHandler(tornado.web.RequestHandler):
    def __init__(self, application, request, **kwargs):
        super(BaseHandler, self).__init__(application, request, **kwargs)
        
    
class TimeHandler(BaseHandler):
    def get(self):
        values = parse_sentence(self.get_argument('message'))
        self.set_header("Content-Type", "application/json")
        self.write(json_encode(values))

    

def main():
    parse_command_line()
    app = tornado.web.Application(
            [ 
              (r"/parse_time",TimeHandler),
              ],
            cookie_secret="cook_{}".format(random.randint(1,1000000)),
            login_url="/auth/login",
            template_path=os.path.join(os.path.dirname(__file__), "templates"),
            static_path=os.path.join(os.path.dirname(__file__), "static"),
            xsrf_cookies=False,
            debug=True,
            users=dict(),
            client_users=dict(),
            clients=[]
        )
    app.listen(options.port)
    ioLoop=tornado.ioloop.IOLoop.instance()
    print 'Starting server'
    ioLoop.start()


if __name__ == "__main__":
    main()
