
==================


Bitmerchant &mdash; A free, self-hostable Bitcoin payment processor.
==========


Bitmerchant is a full [Bitcoinj](https://github.com/bitcoinj/bitcoinj)-based merchant-services platform. It lets you accept bitcoin payments or donations on your site, without having to go through an intermediary service like coinbase or bitpay; the purchases or donations go directly into a wallet that **you control**. 

For more information, head over to
TODO bitmerchant.github.io.

## Installation

To install Bitmerchant, make sure you have both java 8, and maven installed. Then run the following commands:
```
git clone https://github.com/tchoulihan/bitmerchant
cd bitmerchant
./install.sh
```


To run Bitmerchant
<pre>
java -jar target/bitmerchant.jar (optional parameters)
parameters:
-testnet : run on the bitcoin testnet3
-deleteDB : delete the local database before starting

</pre>

## Features include
* A fully-functioning bitcoin [wallet](TODO), in a slick bootstrap-based WEBGUI. 
* A well-documented [API].
* A slick [payment-button generator](TODO) that can create orders using your own *native currency*.
* Refund orders at the [click of a button](TODO).
* Uses the BIP70 Payment protocol to ensure correct payment amounts, and refund addresses.
* Implement your own SSL certs.


## Documentation

For more information, follow here: http://bitmerchant.github.io