
class Input:
    
    config = ""
    
    # Input parameters: config=prod | demo
    def __init__(self, args):
        for arg in args[1:]:
            if "config" in arg:
                Input.config = arg.split("=")[1]