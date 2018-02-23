# KuKuMi
Connect Xiaomi product to SmartThings
- Mi Remote

Installation
1. Run a 'KuKuMi' server using docker image

    ```
        # docker run --name=KuKuMi --net=host turlvo/kukumi        
    ```

2. connect 'IP:8484/miremote' through Web browser
- add a 'Mi Remote'(now manually)
can discover 'Mi Remote' device 

    ```
        # docker exec KuKuMi mirobo discover        
    ```

- add a command(manual/learning)

3. Install 'KuKuMi' smartapp and DTH
https://github.com/turlvo/KuKuMi

