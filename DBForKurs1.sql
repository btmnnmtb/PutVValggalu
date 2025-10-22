
CREATE TABLE manufacturers (
    manufacturer_id SERIAL PRIMARY KEY,
    manufacturer_name VARCHAR(100) NOT NULL,
	    manufacturer_price NUMERIC(10,2)
);

CREATE TABLE brands (
    brand_id SERIAL PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL,
    brand_price NUMERIC(10,2)
);

CREATE TABLE cosmetic_types (
    cosmetic_type_id SERIAL PRIMARY KEY,
    cosmetic_type_name VARCHAR(100) NOT NULL,
    cosmetic_type_price NUMERIC(10,2)
);

CREATE TABLE quality_certificates (
    certificate_id SERIAL PRIMARY KEY,
    certificate_name VARCHAR(100) not null,
    certificate_price NUMERIC(10,2),
   
);
INSERT INTO manufacturers (manufacturer_name, manufacturer_price) VALUES
('Acme Labs', 0.00),
('Natura LLC', 0.00),
('CosmoWorks', 0.00);

-- Brands (3)
INSERT INTO brands (brand_name, brand_price) VALUES
('VelvetSkin', 0.00),
('PureGlow',   0.00),
('UrbanLeaf',  0.00);

-- Cosmetic types (3)
INSERT INTO cosmetic_types (cosmetic_type_name, cosmetic_type_price) VALUES
('Cream',   0.00),
('Serum',   0.00),
('Shampoo', 0.00);
INSERT INTO quality_certificates (certificate_name, certificate_price, metal_name) VALUES
('ISO 22716',     0.00, NULL),
('FDA Registered',0.00, NULL),
('Vegan Certified',0.00, NULL);

CREATE TABLE product_statuses (
    product_status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(100) not null
);


CREATE TABLE cosmetic_items (
    cosmetic_item_id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    manufacturer_id int,
    brand_id int,
    cosmetic_type_id int,
    certificate_id int,
    image_path VARCHAR(255),
    description TEXT,
    product_status_id int,
    quantity int,
    price NUMERIC(10,2),

    FOREIGN KEY (manufacturer_id) REFERENCES manufacturers(manufacturer_id),
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id),
    FOREIGN KEY (cosmetic_type_id) REFERENCES cosmetic_types(cosmetic_type_id),
    FOREIGN KEY (certificate_id) REFERENCES quality_certificates(certificate_id),
    FOREIGN KEY (product_status_id) REFERENCES product_statuses(product_status_id)
);



CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL
);
insert into roles (role_name)
values
('Пользователь'), 
('Администратор'), 
('Менаджер'), 
('Сотрудник склада'); 


CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    login VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id int REFERENCES roles(role_id)
);
insert into users (login, password_hash , role_id)
values 
('log' , 'dsgsd' , 1), 
('log2' , 'dsgsd' , 2), 
('log3' , 'dsgsd' , 3);


CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    user_id int REFERENCES users(user_id),
    order_date DATE,
    status VARCHAR(50) not null,
    total NUMERIC(10,2)
);

CREATE TABLE order_items (
    order_item_id SERIAL PRIMARY KEY,
    order_id int REFERENCES orders(order_id),
    cosmetic_item_id INTEGER REFERENCES cosmetic_items(cosmetic_item_id),
    price NUMERIC(10,2),
    quantity int
);


CREATE TABLE carts (
    cart_id SERIAL PRIMARY KEY,
    user_id int REFERENCES users(user_id)
);

CREATE TABLE cart_items (
    cart_item_id SERIAL PRIMARY KEY,
    cart_id int REFERENCES carts(cart_id),
    cosmetic_item_id int REFERENCES cosmetic_items(cosmetic_item_id),
    quantity int
);



CREATE TABLE favourites (
    favourite_id SERIAL PRIMARY KEY,
    user_id int REFERENCES users(user_id),
    cosmetic_item_id int REFERENCES cosmetic_items(cosmetic_item_id)
);



CREATE TABLE comments (
    comment_id SERIAL PRIMARY KEY,
    user_id int REFERENCES users(user_id),
    cosmetic_item_id int REFERENCES cosmetic_items(cosmetic_item_id),
    comment_text TEXT not null,
    rating int,
    comment_date TIMESTAMP DEFAULT now()
);


CREATE TABLE actions (
    action_id SERIAL PRIMARY KEY,
    action_name VARCHAR(100) NOT NULL
);

CREATE TABLE logs (
    log_id SERIAL PRIMARY KEY,
    action_id int REFERENCES actions(action_id),
    user_id int REFERENCES users(user_id),
    log_date TIMESTAMP DEFAULT now()
);



create view cosmetic_table as 
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
    ps.status_name
FROM cosmetic_items ci
LEFT JOIN manufacturers m ON m.manufacturer_id = ci.manufacturer_id
LEFT JOIN brands b ON b.brand_id = ci.brand_id
LEFT JOIN cosmetic_types t ON t.cosmetic_type_id = ci.cosmetic_type_id
LEFT JOIN quality_certificates qc ON qc.certificate_id = ci.certificate_id
LEFT JOIN product_statuses ps ON ps.product_status_id = ci.product_status_id; 

CREATE OR REPLACE VIEW users_table AS
SELECT
    us.user_id,
    us.login as "Логин",
    us.password_hash as "Пароль",
    rs.role_name as "Роль"
FROM users us
LEFT JOIN roles rs ON rs.role_id = us.role_id;


CREATE OR REPLACE FUNCTION add_user(p_login VARCHAR, p_password VARCHAR, p_role_id INT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO users (login, password_hash, role_id)
    VALUES (p_login, p_password, p_role_id);
END;
$$ LANGUAGE plpgsql;

select add_user('testlog1', '122' , 3)