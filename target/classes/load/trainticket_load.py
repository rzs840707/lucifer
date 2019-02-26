from locust import TaskSet, HttpLocust, task
from locust.clients import HttpSession
import random


class NormalUserTasks(TaskSet):
    session = None
    loginId = None
    loginToken = None
    orderListToPay = []
    orderListPaid = []
    orderListCollected = []
    orderListEntered = []

    # login as normal user
    def setup(self):
        NormalUserTasks.session = HttpSession(MyLocust.host)

        # get verification
        uri = '/verification/generate'
        NormalUserTasks.session.get(uri)

        # login
        username = 'fdse_microservices@163.com'
        password = 'DefaultPassword'
        verification = '1234'

        load = {"email": username,
                "password": password,
                "verificationCode": verification}
        uri = '/login'

        # response = self.client.post(uri, json=load, cookies=NormalUserTasks.cookies)
        # print(response.cookies, response.headers)
        response = NormalUserTasks.session.post(uri, json=load)

        NormalUserTasks.loginId = response.cookies["loginId"]
        NormalUserTasks.loginToken = response.cookies["loginToken"]
        # print(response.json())

    # book ticket
    @task(10)
    def case1(self):
        # query travel
        params = {
            "startingPlace": "Shang Hai",
            "endPlace": "Su Zhou",
            "departureTime": "2019-01-02"
        }
        uri = '/travel/query'
        response = NormalUserTasks.session.post(uri, json=params)
        # print (response.text)

        # query food
        params = {
            "date": "2019-01-02",
            "startStation": "Shang Hai",
            "endStation": "Su Zhou",
            "tripId": "D1345"
        }
        uri = '/food/getFood'
        response = NormalUserTasks.session.post(uri, json=params)
        # print (response.text)

        # query assurance
        uri = '/assurance/getAllAssuranceType'
        response = NormalUserTasks.session.get(uri)
        # print (response.text)

        # query contact
        uri = '/contacts/findContacts'
        response = NormalUserTasks.session.get(uri)
        # print (response.text)

        # create contact?
        params = {
            "name": "",
            "documentType": "2",
            "documentNumber": "",
            "phoneNumber": ""
        }
        uri = '/contacts/create'
        response = NormalUserTasks.session.post(uri, json=params)
        # print (response.text)

        # book ticket
        params = {"contactsId": "4d2a46c7-71cb-4cf1-a5bb-b68406d9da6f", "tripId": "D1345", "seatType": "3",
                  "date": "2020-01-01", "from": "Shang Hai", "to": "Su Zhou", "assurance": "1", "foodType": 2,
                  "stationName": "suzhou", "storeName": "Roman Holiday", "foodName": "Oily bean curd", "foodPrice": 2,
                  "handleDate": "2020-01-02", "consigneeName": "ccx", "consigneePhone": "13718927394",
                  "consigneeWeight": 12, "isWithin": False}
        uri = '/preserve'
        # response = self.client.post(uri, json=params, cookies=NormalUserTasks.cookies,
        #                             proxies={'http': '127.0.0.1:8080'})

        response = NormalUserTasks.session.post(uri, json=params)

        # print(response.text)
        orderId = response.json()["order"]["id"]
        NormalUserTasks.orderListToPay.append(orderId)
        print ("preserve", orderId)
        if len(NormalUserTasks.orderListToPay) > 1000:
            NormalUserTasks.orderListToPay = NormalUserTasks.orderListToPay[:1000]
        # print(NormalUserTasks.orderListToPay)

    # pay
    @task(10)
    def case2(self):
        # query list
        params = {"enableStateQuery": False, "enableTravelDateQuery": False, "enableBoughtDateQuery": False,
                  "travelDateStart": None, "travelDateEnd": None, "boughtDateStart": None, "boughtDateEnd": None}
        uri = '/order/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)
        uri = '/orderOther/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

        try:
            if len(NormalUserTasks.orderListToPay) > 0:
                orderId = NormalUserTasks.orderListToPay.pop()
                params = {
                    "orderId": orderId,
                    "tripId": "D1345"
                }
                uri = '/inside_payment/pay'
                response = NormalUserTasks.session.post(uri, json=params)
                print("paid", orderId, response.text)

                NormalUserTasks.orderListPaid.append(orderId)
                if len(NormalUserTasks.orderListPaid) > 1000:
                    NormalUserTasks.orderListPaid = NormalUserTasks.orderListPaid[:1000]
            else:
                print("order to pay list empty")
        except Exception as e:
            print (e)

    # change
    @task(1)
    def case3(self):
        # query list
        params = {"enableStateQuery": False, "enableTravelDateQuery": False, "enableBoughtDateQuery": False,
                  "travelDateStart": None, "travelDateEnd": None, "boughtDateStart": None, "boughtDateEnd": None}
        uri = '/order/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)
        uri = '/orderOther/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

        try:
            if len(NormalUserTasks.orderListToPay) > 1:
                # change trip
                idx = random.randint(0, len(NormalUserTasks.orderListPaid))
                orderId = NormalUserTasks.orderListPaid[idx]
                params = {
                    "orderId": orderId,
                    "oldTripId": "D1345",
                    "tripId": "D1345",
                    "seatType": 2,
                    "date": "2020-01-02"
                }
                uri = '/rebook/rebook'
                response = NormalUserTasks.session.post(uri, json=params)
                print ("rebook", orderId)
                # print(response.text)

                # charge difference price
                uri = '/rebook/payDifference'
                response = NormalUserTasks.session.post(uri, json=params)
                print ("pay difference", orderId)
                # print(response.text)
            else:
                print ("no order paid for change")
        except Exception as e:
            print (e)

    # consign
    @task(1)
    def case4(self):
        # query list
        params = {"enableStateQuery": False, "enableTravelDateQuery": False, "enableBoughtDateQuery": False,
                  "travelDateStart": None, "travelDateEnd": None, "boughtDateStart": None, "boughtDateEnd": None}
        uri = '/order/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        uri = '/orderOther/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)

        params = {"accountId": NormalUserTasks.loginId, "handleDate": "2020-01-02",
                  "targetDate": "2020-01-02 18:33:15", "from": "Shang Hai", "to": "Su Zhou", "consignee": "ccx",
                  "phone": "126",
                  "weight": "12", "isWithin": False}
        uri = '/consign/insertConsign'
        print ("consign")
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

    # cancel ticket
    @task(1)
    def case5(self):
        # query list
        params = {"enableStateQuery": False, "enableTravelDateQuery": False, "enableBoughtDateQuery": False,
                  "travelDateStart": None, "travelDateEnd": None, "boughtDateStart": None, "boughtDateEnd": None}
        uri = '/order/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)
        uri = '/orderOther/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

        try:
            if len(NormalUserTasks.orderListToPay) > 1:
                orderId = NormalUserTasks.orderListToPay.pop()
                params = {
                    "orderId": orderId
                }
                uri = '/cancelOrder'
                response = NormalUserTasks.session.post(uri, json=params)
                print ("cancel", orderId)
                # print(response.text)
            else:
                print("no order to cancel")
        except Exception as e:
            print (e)

    # query consign list
    @task(1)
    def case6(self):
        uri = '/consign/findByAccountId/' + NormalUserTasks.loginId
        response = NormalUserTasks.session.get(uri)
        print("query consign list")
        # print(response.text)

    # query by condition
    @task(1)
    def case7(self):
        params = {"startingPlace": "Nan Jing", "endPlace": "Shang Hai", "departureTime": "2020-01-02"}
        uri = '/travelPlan/getMinStation'
        response = NormalUserTasks.session.post(uri, json=params)
        print(response.text)

    # query by condition
    @task(1)
    def case8(self):
        params = {"startingPlace": "Nan Jing", "endPlace": "Shang Hai", "departureTime": "2020-01-02"}
        uri = '/travelPlan/getCheapest'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

    # query by condition
    @task(1)
    def case9(self):
        params = {"startingPlace": "Nan Jing", "endPlace": "Shang Hai", "departureTime": "2020-01-02"}
        uri = '/travelPlan/getQuickest'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

    # print voucher
    @task(10)
    def case10(self):
        # query list
        params = {"enableStateQuery": False, "enableTravelDateQuery": False, "enableBoughtDateQuery": False,
                  "travelDateStart": None, "travelDateEnd": None, "boughtDateStart": None, "boughtDateEnd": None}
        uri = '/order/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)
        uri = '/orderOther/queryForRefresh'
        response = NormalUserTasks.session.post(uri, json=params)
        # print(response.text)

        try:
            if len(NormalUserTasks.orderListEntered) > 0:
                orderId = random.choice(NormalUserTasks.orderListEntered)
                params = {"orderId": orderId, "type": 1}
                uri = '/getVoucher'
                print("print voucher", orderId)
                response = NormalUserTasks.session.post(uri, json=params)
                # print(response.text)
            else:
                print("no entered order to print voucher")
        except Exception as e:
            print (e)

    # collect ticket
    @task(10)
    def case11(self):
        try:
            if len(NormalUserTasks.orderListPaid) > 0:
                orderId = NormalUserTasks.orderListPaid.pop()
                params = {"orderId": orderId}
                uri = '/execute/collected'

                print("collect ticket", orderId)
                response = NormalUserTasks.session.post(uri, json=params)
                print(response.text)

                NormalUserTasks.orderListCollected.append(orderId)
            else:
                print("no paid ticket to collect")
        except Exception as e:
            print (e)

    # enter station
    @task(10)
    def case12(self):
        try:
            if len(NormalUserTasks.orderListCollected) > 0:
                orderId = NormalUserTasks.orderListCollected.pop()
                params = {"orderId": orderId}
                uri = '/execute/execute'

                print("enter", orderId)
                response = NormalUserTasks.session.post(uri, json=params)
                print(response.text)

                NormalUserTasks.orderListEntered.append(orderId)
            else:
                print("no collect ticket to enter")
        except Exception as e:
            print(e)

    # login out
    def teardown(self):
        load = {
            "id": NormalUserTasks.loginId,
            "token": NormalUserTasks.loginToken
        }
        uri = '/logout'
        NormalUserTasks.session.post(uri, json=load)


class MyLocust(HttpLocust):
    task_set = NormalUserTasks
    min_wait = 1000
    max_wait = 15000
    weight = 1  # used by muti-locust
    host = "http://133.133.134.183:31380"
