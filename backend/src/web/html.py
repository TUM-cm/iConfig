
class Html():
    
    def __init__(self, web_application):
        self.web_application = web_application
    
    def create_table_start(self):
        return "<table>"
    
    def create_table_end(self):
        return "</table>"
    
    def create_table_headline(self, columns):
        table_headline = []
        table_headline.append("<tr>")
        for column in columns:
            table_headline.append("<th>")
            table_headline.append(column.upper())
            table_headline.append("</th>")
        table_headline.append("</tr>")
        return self.web_application.newline.join(table_headline)
    
    def create_begin_table_row(self):
        return "<tr>"
    
    def create_end_table_row(self):
        return "</tr>"
    
    def create_column(self, value):
        return "<td>" + str(value) + "</td>"
    
    def create_link(self, path, name):
        return '<a href="' + path + '">' + name + '</a>'
    
    def create_link_new_window(self, path, name):
        return '<a href="' + path + '" target="_blank">' + name + '</a>'
    
    def create_label(self, name):
        return "<label>" + name + ":</label>"
    
    def create_input_field(self, value, name):
        return '<input type=text value="' + str(value) + '" name="' + name + '"/>'
    
    def create_input_field_ready_only(self, value, name):
        return '<input type=text value="' + str(value) + '" name="' + name + '" readonly="readonly"/>'
    
    def create_line_break(self):
        return "<br/>"
    
    def create_headline(self, size, headline):
        return '<' + size + '>' + headline + '</' + size + '>'
    
    def create_image(self, src, alt):
        return '<img src="' + src + '" alt="' + alt + '">'
    
    def create_dropdown_field(self, name, field_value, values, disabled):
        dropdown = []
        head = ['<select name="']
        head.append(name)
        head.append('"')
        if disabled:
            head.append("disabled")
        head.append(">")
        dropdown.append("".join(head))
        # Set current active value
        for value in values:
            if value.startswith(str(field_value)):
                dropdown.append('<option selected="selected">' + value  + '</option>')
                break
        for value in values:
            dropdown.append('<option value="' + value + '">' + value + "</option>")
        dropdown.append("</select>")
        return self.web_application.newline.join(dropdown)
    
    def create_circle(self, color):
        circle = ['<svg width="100" height="100">']
        circle.append('<circle cx="35" cy="35" r="30"')
        circle.append('stroke="black" stroke-width="2" fill="')
        circle.append(color)
        circle.append('" />')
        circle.append('Sorry, your browser does not support inline SVG.')
        circle.append('</svg>')
        return self.web_application.newline.join(circle)
    