
==================


[Bitmerchant](http://tchoulihan.github.io/bitmerchant/) &mdash; A free, self-hostable Bitcoin payment processor.
==========


Bitmerchant is a full [Bitcoinj](https://github.com/bitcoinj/bitcoinj)-based merchant-services platform. It lets you accept bitcoin payments or donations on your own website, without having to go through an intermediary service like coinbase or bitpay; the purchases or donations go directly into a wallet that **you control**, running on your server.



For more information, head over to http://tchoulihan.github.io/bitmerchant/

## Features include
* A fully-functioning bitcoin [wallet](http://imgur.com/a/laYYn), in a slick bootstrap-based web GUI. 
* A well-documented [API](http://tchoulihan.github.io/bitmerchant/api.html).
* A slick [payment-button generator](http://imgur.com/a/laYYn) that can create orders using your own *native currency*.
* Refund orders at the click of a button.
* Uses the BIP70 Payment protocol to ensure correct payment amounts, and refund addresses.
* Implement your own SSL certs.


## Installation

To install Bitmerchant, make sure you have both java 8, and maven installed. Then run the following commands:
```
git clone https://github.com/tchoulihan/bitmerchant
cd bitmerchant
mvn install
```


To run Bitmerchant

<pre>
java -jar target/bitmerchant.jar [parameters]

or better, use the run script, which also creates a log.out:

./run.sh [parameters]

parameters:
	-testnet  : run on the bitcoin testnet3
	-deleteDB : delete the local database before starting
	-loglevel [INFO,WARN, etc] : Sets the log level
</pre>

If accessing from another machine, vnc to the machine, or use a vpn service, and access either
http://localhost:4567/ , or
https://localhost:4567/ once you've enabled ssl.




## Thanks
* Special thanks to Mike Hearn and Andreas Schildbach for their assistance with BIP70 and refunding orders.

