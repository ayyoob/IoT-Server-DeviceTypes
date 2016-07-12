DHT = require("dht_lib")

dht_data = 2
rightMotorForward = 5
leftMotorForward = 8

rightMotorReverse = 6
leftMotorReverse = 7
gpio.mode(leftMotorForward, gpio.OUTPUT)
gpio.mode(rightMotorForward, gpio.OUTPUT)
gpio.mode(leftMotorReverse, gpio.OUTPUT)
gpio.mode(rightMotorReverse, gpio.OUTPUT)

gpio.mode(leftMotorForward, gpio.LOW)
gpio.mode(rightMotorForward, gpio.LOW)
gpio.mode(leftMotorReverse, gpio.LOW)
gpio.mode(rightMotorReverse, gpio.LOW)

client_connected = false
accessToken = ${DEVICE_TOKEN};
refreshToken = ${DEVICE_REFRESH_TOKEN};
deviceId = ${DEVICE_ID}
publishTopic = "carbon.super/sensebot/${DEVICE_ID}/temperature" 
subscribeTopic = "carbon.super/sensebot/${DEVICE_ID}/command"
mqttIp = ${MQTT_EP}
mqttPort = ${MQTT_PORT}

m = mqtt.Client(deviceId, 120, accessToken, "")

tmr.alarm(0, 10000, 1, function()
    DHT.read(dht_data)

    local t = DHT.getTemperature()

    if t == nil then
        print("Error reading from DHTxx")
    else
        if (client_connected) then
            local temperaturePayload = "{event:{metaData:{owner:\"${DEVICE_OWNER}\",deviceId:\"" .. deviceId .. "\"},payloadData:{temperature:" .. t .. "}}}"
            m:publish(publishTopic, temperaturePayload, 0, 0, function(client)
                print("Published> Temperature: " .. t .. "C")
            end)
        else
            connectMQTTClient()
        end
    end
end)

function connectMQTTClient()
    local ip = wifi.sta.getip()
    if ip == nil then
        print("Waiting for network")
    else
        print("Client IP: " .. ip)
        print("Trying to connect MQTT client")
        m:connect(mqttIp, mqttPort, 0, function(client)
            client_connected = true
            print("MQTT client connected")
            subscribeToMQTTQueue()
        end)
    end
end

function subscribeToMQTTQueue()
    m:subscribe(subscribeTopic, 0, function(client, topic, message)
        print("Subscribed to MQTT Queue")
    end)
    m:on("message", function(client, topic, message)
        print("MQTT message received")
        print(message)
        if message == "forward" then
            gpio.write(rightMotorForward, gpio.HIGH)
            gpio.write(leftMotorForward, gpio.HIGH)
            gpio.write(rightMotorReverse, gpio.LOW)
            gpio.write(leftMotorReverse, gpio.LOW)
        end

        if message == "reverse" then
            gpio.write(rightMotorForward, gpio.LOW)
            gpio.write(leftMotorForward, gpio.LOW)
            gpio.write(rightMotorReverse, gpio.HIGH)
            gpio.write(leftMotorReverse, gpio.HIGH)
        end

        if message == "left" then
            gpio.write(leftMotorForward, gpio.LOW)
            gpio.write(rightMotorForward, gpio.HIGH)
            gpio.write(leftMotorReverse, gpio.LOW)
            gpio.write(rightMotorReverse, gpio.LOW)
        end

        if message == "right" then
            gpio.write(leftMotorForward, gpio.HIGH)
            gpio.write(rightMotorForward, gpio.LOW)
            gpio.write(leftMotorReverse, gpio.LOW)
            gpio.write(rightMotorReverse, gpio.LOW)
        end

        if message == "stop" then
            gpio.write(leftMotorForward, gpio.LOW)
            gpio.write(rightMotorForward, gpio.LOW)
            gpio.write(leftMotorReverse, gpio.LOW)
            gpio.write(rightMotorReverse, gpio.LOW)
        end
    end)
    m:on("offline", function(client)
        print("Disconnected")
        client_connected = false
    end)
end