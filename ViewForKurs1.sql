CREATE OR REPLACE view cosmetic_table as 
SELECT 
    ci.cosmetic_item_id,
    ci.item_name,
    ci.price,
    ci.quantity,
    ci.image_path,
    ci.description,
    m.manufacturer_name,
    b.brand_name,
    t.cosmetic_type_name,
    qc.certificate_name,
    ps.status_name,
	ci.rating
FROM cosmetic_items ci
LEFT JOIN manufacturers m ON m.manufacturer_id = ci.manufacturer_id
LEFT JOIN brands b ON b.brand_id = ci.brand_id
LEFT JOIN cosmetic_types t ON t.cosmetic_type_id = ci.cosmetic_type_id
LEFT JOIN quality_certificates qc ON qc.certificate_id = ci.certificate_id
LEFT JOIN product_statuses ps ON ps.product_status_id = ci.product_status_id; 
select * from cosmetic_table
select *from users_table

CREATE OR REPLACE VIEW users_table AS
SELECT
    us.user_id,
    us.login as "Логин",
    us.password_hash as "Пароль",
    rs.role_name as "Роль"
FROM users us
LEFT JOIN roles rs ON rs.role_id = us.role_id;


select * from users
select * from cart_items
create or replace view cart_items_view as 
select 
	ci.cart_item_id,
	ci.cart_id, 
	cosi.item_name as "Название изделия" , 
	ci.quantity as "Количестов"
from cart_items ci
left join cosmetic_items cosi on cosi.cosmetic_item_id = ci.cosmetic_item_id

select * from cart_items_view
select * from carts

