from locust import TaskSet, HttpLocust, task


class MyTaskSet(TaskSet):
    client_id = 0

    def on_start(self):
        MyTaskSet.client_id += 1
        print "start client {0}".format(MyTaskSet.client_id)

    @task(1)
    def circuitbreaker(self):
        self.client.get("/productpageForCircuitBreaker")

    # @task(1)
    # def timeout(self):
    #     self.client.get("/productpageForTimeout")
    #
    # @task(1)
    # def retry(self):
    #     self.client.get("/productpageForRetry")
    #
    # @task(1)
    # def bulkhead(self):
    #     self.client.get("/productpageForBulkhead")

    @task(1)
    def exp(self):
        self.client.get("/productpageForExp")

    def on_stop(self):
        MyTaskSet.client_id -= 1
        print "stop client {0}".format(MyTaskSet.client_id)


class MyLocust(HttpLocust):
    # best user counts = 30

    task_set = MyTaskSet
    # min_wait = 5000
    # max_wait = 15000
    min_wait = 1000
    max_wait = 1000
    weight = 1  # used by muti-locust
    host = "http://localhost:30036"
