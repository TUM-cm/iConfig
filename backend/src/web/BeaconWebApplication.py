import cherrypy
import os
import datetime
import utils.encryption

from utils.url import Url
from html import Html
from utils.encryption import AESCipher

class BeaconWebApplication(object):
    
    def __init__(self, db_beacon_web_application, db_beacon_api, env):
        self.db_beacon_web_application = db_beacon_web_application
        self.db_beacon_api = db_beacon_api        
        self.env = env
        self.html = Html(self)
        self.url_service = Url(self.db_beacon_web_application.get_field("url shortener"),
                               self.db_beacon_web_application.get_field("url shortener key"))        
        self.selected_beacon = None
        self.identifier = self.db_beacon_web_application.get_field("identifier")
        self.picture_identifier = self.db_beacon_web_application.get_field("picture identifier")
        self.form_template = self.db_beacon_web_application.get_field("form")
        self.table_template = self.db_beacon_web_application.get_field("table")
        self.fields = self.create_fields(self.form_template)
        self.readonly = self.db_beacon_web_application.get_field("read-only")
        self.room_finder = self.db_beacon_web_application.get_field("roomfinder")
        self.advertisement_rate = self.db_beacon_web_application.get_field("advertisement rate")
        self.transmission_power = self.db_beacon_web_application.get_field("transmission power")
        self.drop_down_fields_transmission_power = self.db_beacon_web_application.get_field("drop down transmission power")
        self.drop_down_fields_advertisement_rate = self.db_beacon_web_application.get_field("drop down advertisement rate")
        self.image_path = self.db_beacon_web_application.get_field("image path")
        self.advertisement_rate_converter = self.db_beacon_web_application.get_field("advertisement rate converter")
        self.transmission_power_converter = self.db_beacon_web_application.get_field("transmission power converter")
        self.advertisement_rate_converter_fields = self.db_beacon_web_application.get_field("advertisement rate converter fields")
        self.transmission_power_converter_fields = self.db_beacon_web_application.get_field("transmission power converter fields")
        self.maintenance_field = self.db_beacon_web_application.get_field("maintenance field", "web api")
        self.timestamp_field = self.db_beacon_web_application.get_field("timestamp field", "web api")
        self.len_s_beacon_id = self.db_beacon_web_application.get_field("len s beacon id")        
        self.action_update_beacon = "/update_beacon/"
        self.action_edit_beacon = "/edit_beacon/"
        self.newline = self.db_beacon_web_application.get_field("newline")
        self.update_action = False
        self.status = ""
        self.aes_cipher = AESCipher.keyFromVariable(utils.encryption.key)
    
    @cherrypy.expose
    def index(self):
        self.beacons = list(self.db_beacon_web_application.get_all_beacons())
        columns = self.create_table_columns(self.table_template)
        rows = self.create_table_rows(self.beacons, self.identifier, self.table_template)
        if self.update_action:
            self.update_action = False
        else:
            self.status = ""
        template = self.env.get_template('listBeacons.html')
        return template.render(title='List Beacons', columns=columns, rows=rows, status=self.status)
    
    @cherrypy.expose
    def edit_beacon(self, **beacon):
        self.selected_beacon = self.get_beacon(beacon[self.identifier])
        form = self.create_form(self.selected_beacon, self.form_template, self.identifier, self.readonly,
                                self.drop_down_fields_transmission_power, self.drop_down_fields_advertisement_rate)
        template = self.env.get_template('beaconDetails.html')
        return template.render(title="Edit Beacon Details", action=self.action_update_beacon, form=form)
    
    @cherrypy.expose
    def update_beacon(self, **beacon):
        self.update_action = self.update_object(beacon)
        if self.update_action:
            self.status = "Updated beacon: " + self.selected_beacon["sBeacon"]["id"]
            url = self.selected_beacon["eddystone"]["url"]
            if self.get_url_service().check(url):
                new_url = self.get_url_service().shorten(url)
                self.selected_beacon["eddystone"]["url"] = new_url
                self.status += ", url: " + url + " converted to: " + new_url
            self.db_beacon_web_application.update_beacon(self.selected_beacon, self.identifier)
        # Redirect to root page
        raise cherrypy.HTTPRedirect("/")
    
    def create_table_rows(self, beacons, identifier, table_template):
        table = []
        self.beacons_to_update = [beacon[self.identifier] for beacon in self.db_beacon_api.get_beacon_configs_to_update()]
        for beacon in beacons:
            # Start row
            table.append(self.get_html().create_begin_table_row())            
            for entry in table_template:
                field = self.get_field(entry)                
                if field == "action":                    
                    link_content = self.action_edit_beacon + "?" + identifier + "=" + self.get_value(beacon, identifier)
                    table.append(self.get_html().create_column(self.get_html().create_link(link_content, "Edit")))                    
                elif field == "nearest room" and field in beacon:
                    room = self.get_value(beacon, field)
                    if room != None:
                        building = self.get_building(room)
                        if building:
                            link_path = self.create_roomfinder_link(room, building)
                            table.append(self.get_html().create_column(self.get_html().create_link_new_window(link_path, room)))
                        else:
                            table.append(self.get_html().create_column(room))
                    else:
                        table.append(self.get_html().create_column("-"))
                elif field == "beaconPlace" and self.picture_identifier in beacon:
                    image_id = self.get_value(beacon, self.picture_identifier).replace(":", "")
                    image_filename = image_id
                    if self.is_valid_path(self.image_path):
                        found_filename = self.find_filename(self.image_path, image_id)
                        if found_filename:
                            image_filename = found_filename
                    src = "/images/" + image_filename
                    alt = "image"
                    image = self.get_html().create_image(src, alt)
                    table.append(self.get_html().create_column(image))                
                elif "status," in field:
                    if self.get_value(beacon, field):
                        circle = self.get_html().create_circle("green")
                    else:
                        circle = self.get_html().create_circle("red")                    
                    table.append(self.get_html().create_column(circle))
                elif field == "eddystone,url":
                    url = self.get_value(beacon, field)
                    if url is not None:
                        expanded_url = self.get_url_service().expand(url)
                    else:
                        expanded_url = "-"
                    table.append(self.get_html().create_column(expanded_url))
                elif field == "updateStatus" and self.identifier in beacon:
                    value = self.get_update_status(beacon)
                    table.append(self.get_html().create_column(value))                    
                elif field == self.maintenance_field and self.maintenance_field in beacon:
                    sub_table = self.create_maintenance_table(beacon, only_latest_value=True)
                    table.append(self.get_html().create_column(sub_table))
                else:
                    value = self.get_value(beacon, field)
                    if value == None:
                        value = "-"
                    table.append(self.get_html().create_column(value))
            # End row
            table.append(self.get_html().create_end_table_row())
        return self.newline.join(table)
    
    def create_form(self, beacon, form_template, identifier, readonly, drop_down_field_transmission_power, drop_down_field_advertisement_rate):
        form = []
        for entry in form_template:
            if "headline" in entry:
                elements = entry.split(":")
                size = elements[1]
                headline = elements[2]
                form.append(self.get_html().create_headline(size, headline))
            elif "field" in entry:
                field = self.get_field(entry)
                label = self.get_label(entry)
                group = self.get_group(entry)
                
                general_group = (group == "General")
                if not general_group:
                    status = beacon["status"]                    
                    disabled = not status[group]
                
                if field != self.maintenance_field:
                    form.append(self.get_html().create_label(label))
                
                if field == "updateStatus" and self.identifier in beacon:
                    value = self.get_update_status(beacon)
                    form.append(self.get_html().create_input_field_ready_only(value, field))
                elif field == "sBeacon,id":
                    value = self.get_value(beacon, field)
                    if not self.is_valid_sbeacon_id(value):
                        form.append(self.get_html().create_input_field(self.get_value(beacon, field), field))
                    else:
                        form.append(self.get_html().create_input_field_ready_only(self.get_value(beacon, field), field))
                elif field == "new password":
                    value = self.get_value(beacon, field)                    
                    if value == None or (value != None and len(value)==0):
                        value = self.get_value(beacon, "password")
                    value = self.get_cipher().decrypt(value)
                    if self.is_broken(beacon):
                        form.append(self.get_html().create_input_field_ready_only(value, field))
                    else:
                        form.append(self.get_html().create_input_field(value, field))  
                elif field == self.maintenance_field:
                    table = self.create_maintenance_table(beacon)
                    form.append(table)
                elif field == identifier:
                    form.append(self.get_html().create_input_field_ready_only(self.get_value(beacon, field), field))
                elif field in readonly:
                    form.append(self.get_html().create_input_field_ready_only(self.get_value(beacon, field), field))
                elif field in drop_down_field_transmission_power:
                    form.append(self.get_html().create_dropdown_field(field, self.get_value(beacon, field), self.transmission_power, disabled))
                elif field in drop_down_field_advertisement_rate:
                    form.append(self.get_html().create_dropdown_field(field, self.get_value(beacon, field), self.advertisement_rate, disabled))
                else:
                    value = self.get_value(beacon, field)
                    if field == "eddystone,url":
                        value = self.get_url_service().expand(value)                    
                    if general_group or status[group]:
                        form.append(self.get_html().create_input_field(value, field))
                    else:
                        form.append(self.get_html().create_input_field_ready_only(value, field))
                
                form.append(self.get_html().create_line_break())
        return self.newline.join(form)
    
    def get_update_status(self, beacon):
        if beacon[self.identifier] in self.beacons_to_update:
            return "wait for update"
        else:
            return "up-to-date"
    
    def create_maintenance_table(self, beacon, only_latest_value=False):
        maintenance = beacon[self.maintenance_field]
        columns = maintenance[0].keys()
        columns.insert(0, columns.pop(columns.index(self.timestamp_field)))        
        values = []
        if only_latest_value:
            values.append(maintenance[-1])
        else:
            values = maintenance
        sub_table = []
        sub_table.append(self.get_html().create_table_start())
        sub_table.append(self.get_html().create_table_headline(columns))
        for entry in values:
            sub_table.append(self.get_html().create_begin_table_row())
            for column in columns:
                value = entry[column]
                if column == self.timestamp_field:
                    value = self.create_date(value)
                sub_table.append(self.get_html().create_column(value))
            sub_table.append(self.get_html().create_end_table_row())
        sub_table.append(self.get_html().create_table_end())
        return self.newline.join(sub_table)
    
    def create_fields(self, form_template):
        fields = []
        for entry in form_template:
            if "field" in entry:
                elements = entry.split(":")
                fields.append(elements[1])
        return fields
    
    def is_broken(self, beacon):
        # If at least one status is true, then not broken, otherwise broken
        return not any(beacon["status"].values())
    
    def create_table_columns(self, table_template):
        columns = []
        for entry in table_template:
            columns.append(self.get_label(entry))
        return columns
    
    def get_building(self, room):
        elements = room.split(".")
        if len(elements) > 2:
            return room.split(".")[1]
        else:
            return None
    
    def create_roomfinder_link(self, room, building):
        link = self.room_finder
        return link.replace("{room}", room).replace("{building}", building)
    
    def is_valid_path(self, path):
        return os.path.isdir(path)
    
    def find_filename(self, directory, filename):
        for f in os.listdir(directory):
            if filename in f:
                return f
    
    def get_form_element(self, entry, keyword):
        elements = entry.split(":")
        for element in elements:
            if keyword in element:
                return element.split("=")[1]
    
    def get_field(self, entry):
        keyword = "field"
        return self.get_form_element(entry, keyword)
    
    def get_label(self, entry):
        keyword = "label"
        return self.get_form_element(entry, keyword)
    
    def get_group(self, entry):
        keyword = "group"
        return self.get_form_element(entry, keyword)
    
    def is_valid_sbeacon_id(self, sbeacon_id):
        if sbeacon_id != None:
            if len(sbeacon_id) == self.len_s_beacon_id and sbeacon_id.isalnum():
                return True
            else:
                return False
        else:
            return False
    
    def converter(self, old_input_list, old_value, select_list):
        for idx, input_value in enumerate(old_input_list):
            if input_value == old_value:
                return select_list[idx]
    
    def create_date(self, unix_timestamp):
        return datetime.datetime.fromtimestamp(unix_timestamp)
    
    def update_object(self, beacon):
        updated = False
        for key_list in beacon.keys():
            new_value = beacon[key_list]
            # Convert special values
            if key_list in self.advertisement_rate_converter_fields:
                new_value = self.converter(self.advertisement_rate, new_value, self.advertisement_rate_converter)
            elif key_list in self.transmission_power_converter_fields:
                new_value = self.converter(self.transmission_power, new_value, self.transmission_power_converter)
            keys = key_list.split(",")            
            result = self.update(self.selected_beacon, keys, new_value)
            if result and not updated:
                updated = True
        return updated
    
    def update(self, beacon, keys, new_value):
        key = keys[0]        
        if key in beacon:         
            if isinstance(beacon[key], dict):
                del keys[0]
                return self.update(beacon[key], keys, new_value)
            else:
                if key == "new password":
                    new_password = self.get_cipher().decrypt(beacon[key])                          
                    password = self.get_cipher().decrypt(beacon["password"])      
                    if len(new_password) > 0:
                        if new_password != new_value and password != new_value:
                            beacon["new password"] = self.get_cipher().encrypt(new_value)
                            return True
                        elif password == new_value:
                            beacon["new password"] = ""
                            return True
                        else:
                            return False                    
                    elif password != new_value:
                        beacon["new password"] = self.get_cipher().encrypt(new_value)
                        return True
                    else:
                        return False
                elif type(new_value)(beacon[key]) != new_value:
                    beacon[key] = new_value
                    return True
                else:
                    return False
        else:
            return False
    
    def get_beacon(self, mac):
        for beacon in self.beacons:
            if beacon[self.identifier] in mac:
                return beacon
    
    def get_value(self, data, key_list):
        keys = key_list.split(",")
        for key in keys:            
            if data != None and key in data.keys():
                data = data[key]
            else:
                return None
        return data
    
    def get_html(self):
        return self.html
    
    def get_url_service(self):
        return self.url_service
    
    def get_cipher(self):
        return self.aes_cipher
    