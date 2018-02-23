import sqlite3

class Device:
    def __init__ (self, id, ip, token):
        self.id = id
        self.ip = ip
        self.token = token
        self.command_name = None
        self.command_code = None

    def get_id(self):
        return self.id

    def get_ip(self):
        return self.ip

    def get_token(self):
        return self.token

    def add_device(self):
        try:
            db = sqlite3.connect('./MiRemote.db')
            cur = db.cursor()

            q = "insert into tblDevCommands values(?, ?, ?, ?, ?)"

            cur.execute(q, (self.id, self.ip, self.token, self.command_name, self.command_code))

            db.commit()
            db.close()
            print("insert success")
        except Exception as err:
            print("error: ", err)

    def delete_device(self):
        try:
            db = sqlite3.connect('./MiRemote.db')
            cur = db.cursor()

            q = "delete from tblDevCommands where id = '%s'" % self.id

            cur.execute(q)

            db.commit()
            db.close()
            print("delete success")
        except Exception as err:
            print("error: ", err)

    @staticmethod
    def search_device(id):
        try:
            db = sqlite3.connect('./MiRemote.db')
            cur = db.cursor()

            q = "select * from tblDevCommands where id = %s" % id

            cur.execute(q)

            result = cur.fetchall()
            print(result)

            db.commit()
            db.close()
            print("select success")
            return result

        except Exception as err:
            print("error: ", err)