mvn install
ps aux | grep -ie bitmerchant-wallet-shaded.jar | awk '{print $2}' | xargs kill -9
nohup java -jar target/bitmerchant-wallet-shaded.jar delete &> log.out &

