CREATE VIEW buttons_view AS 
SELECT
buttons.id as id, type, style, text, name, description, 
callback_url, total_native, iso as native_currency_iso,
variable_price, price_select, price_1, price_2, price_3,
price_4, price_5, buttons.created_at
FROM buttons
left join button_types
on button_types.id = buttons.type_id
left join button_styles
on button_styles.id = buttons.style_id
left join currencies
on currencies.id = buttons.native_currency_id
;

CREATE VIEW orders_view AS 
SELECT
orders.id as id, status, orders.total_satoshis, receive_address,
buttons.name as button_name, buttons.id as button_id, total_native,
iso as native_currency_iso, description as button_description,
transaction_hash, network, 
payment_url, payment_request_url, merchant_data, memo, expire_time, orders.created_at
from orders
left join order_statuses
on order_statuses.id = orders.status_id

left join buttons
on buttons.id = orders.button_id

left join currencies
on currencies.id = buttons.native_currency_id
;
