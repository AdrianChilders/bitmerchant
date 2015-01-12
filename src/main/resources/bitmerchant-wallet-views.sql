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