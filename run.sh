ps aux | grep -ie bitmerchant-shaded.jar | awk '{print $2}' | xargs kill -9
nohup java -jar target/bitmerchant-shaded.jar $@ &> log.out &
