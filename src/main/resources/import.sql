-- import.sql para MySQL - Sistema de Alojamientos (expandido y corregido)
-- Datos de prueba ordenados por dependencias
-- Notas:
--  - Las contraseñas usan el hash BCrypt estándar para "password":
--    $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
--    Por tanto, la contraseña en texto plano de todos los usuarios de este import es: password
--  - La columna comments.host_id proviene del embebido HostReply.hostId y referencia el ID del usuario anfitrión (UserEntity.id),
--    coherente con CommentServiceImpl (accommodation.getHost().getId()).

-- 1) Ciudades (sin dependencias)
INSERT INTO cities (name, country) VALUES ('Medellín', 'Colombia');            -- id: 1
INSERT INTO cities (name, country) VALUES ('Bogotá', 'Colombia');              -- id: 2
INSERT INTO cities (name, country) VALUES ('Cartagena', 'Colombia');           -- id: 3
INSERT INTO cities (name, country) VALUES ('Cali', 'Colombia');                -- id: 4
INSERT INTO cities (name, country) VALUES ('Barcelona', 'España');             -- id: 5
-- Nuevas ciudades
INSERT INTO cities (name, country) VALUES ('Buenos Aires', 'Argentina');       -- id: 6
INSERT INTO cities (name, country) VALUES ('Lima', 'Perú');                    -- id: 7
INSERT INTO cities (name, country) VALUES ('Quito', 'Ecuador');                -- id: 8
INSERT INTO cities (name, country) VALUES ('Santiago', 'Chile');               -- id: 9
INSERT INTO cities (name, country) VALUES ('Ciudad de México', 'México');      -- id: 10

-- 2) Usuarios (sin dependencias)
-- Todos con contraseña en texto plano: password
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Admin Principal', 'admin@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573001234567', '1985-03-15', 'Administrador del sistema', true, true, '2024-01-01 10:00:00', false); -- id: 1
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('María González', 'maria@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573112345678', '1980-07-22', 'Anfitriona experimentada', true, true, '2024-01-15 09:30:00', false);                               -- id: 2
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Carlos Rodríguez', 'carlos@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573223456789', '1975-11-08', 'Propietario de apartamentos turísticos', true, true, '2024-01-20 14:15:00', false);                 -- id: 3
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Ana Martínez', 'ana@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573334567890', '1988-04-12', 'Arquitecta convertida en anfitriona', true, true, '2024-01-25 11:45:00', false);                                -- id: 4
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Laura Pérez', 'laura@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573445678901', '1992-12-05', 'Viajera frecuente', true, true, '2024-02-05 08:30:00', false);                                            -- id: 5
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Diego Ramírez', 'diego@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573556789012', '1989-06-18', 'Fotógrafo profesional', true, true, '2024-02-10 12:15:00', false);                                          -- id: 6
-- Nuevos usuarios (hosts y huéspedes)
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Pedro López', 'pedro@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573667890123', '1983-09-10', 'Chef y anfitrión ocasional', true, true, '2024-02-15 10:00:00', false);                                        -- id: 7
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Sofía Rivas', 'sofia@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573778901234', '1990-02-20', 'Diseñadora de interiores', true, true, '2024-02-18 09:40:00', false);                                         -- id: 8
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Javier Ortega', 'javier@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573889012345', '1986-01-02', 'Anfitrión business traveler', true, true, '2024-02-20 08:20:00', false);                                       -- id: 9
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Valentina Mora', 'valentina@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+573990123456', '1995-10-28', 'Nómada digital', true, true, '2024-02-22 13:30:00', false);                                              -- id: 10
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Miguel Torres', 'miguel@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+574001234567', '1982-05-06', 'Empresario tech', true, true, '2024-02-25 10:10:00', false);                                                -- id: 11
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Camila Restrepo', 'camila@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+574112345678', '1993-03-30', 'Food blogger', true, true, '2024-02-26 07:30:00', false);                                               -- id: 12
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Andrés Salazar', 'andres@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+574223456789', '1991-07-11', 'Amante del trekking', true, true, '2024-02-28 16:45:00', false);                                            -- id: 13
INSERT INTO users (name, email, password, phone, date_of_birth, description, verified, enabled, created_at, deleted) VALUES ('Daniela Patiño', 'daniela@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+574334567890', '1996-09-23', 'Arquitecta junior', true, true, '2024-03-01 09:00:00', false);                                             -- id: 14

-- 2.1) Roles
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (2, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (2, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (3, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (3, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (4, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (4, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (5, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (6, 'USER');
-- Nuevos roles
INSERT INTO user_roles (user_id, role) VALUES (7, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (7, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (8, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (9, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (9, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (10, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (11, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (11, 'HOST');
INSERT INTO user_roles (user_id, role) VALUES (12, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (13, 'USER');
INSERT INTO user_roles (user_id, role) VALUES (14, 'USER');

-- 3) Perfiles de anfitriones (depende de usuarios)
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (2, 'Alojamientos María', true, '2024-01-15 10:00:00');  -- id: 1
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (3, 'Apartamentos Centro', true, '2024-01-20 15:00:00');  -- id: 2
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (4, 'Design Stays', true, '2024-01-25 12:00:00');        -- id: 3
-- Nuevos perfiles
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (7, 'Cocina & Stay', true, '2024-02-15 10:10:00');       -- id: 4
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (9, 'Business Travel Homes', true, '2024-02-20 08:40:00');-- id: 5
INSERT INTO host_profiles (host_id, business_name, verified, created_at) VALUES (11, 'TechLoft', true, '2024-02-25 10:20:00');           -- id: 6

-- 4) Alojamientos (depende de usuarios y ciudades) - con coordenadas embebidas
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Apartamento Moderno en El Poblado', 'Hermoso apartamento en Medellín con vista espectacular', 'Cra 35 #10A-52, El Poblado', 6.2088, -75.5648, 120000.00, 4, true, false, '2024-01-16 10:00:00', 2, 1); -- id: 1
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Casa Colonial en La Candelaria', 'Auténtica casa colonial en Bogotá, totalmente restaurada', 'Calle 12C #4-69, Bogotá', 4.5981, -74.0758, 90000.00, 6, true, false, '2024-01-21 11:30:00', 3, 2);        -- id: 2
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Loft en Zona Rosa', 'Loft de diseño contemporáneo con terraza', 'Cra 13 #85-32, Bogotá', 4.6551, -74.0593, 180000.00, 2, true, false, '2024-01-26 14:15:00', 4, 2);               -- id: 3
-- Nuevos alojamientos
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Cabaña en el Bosque', 'Cabaña acogedora ideal para desconexión', 'Km 12 vía Las Palmas', 6.2123, -75.4976, 150000.00, 5, true, false, '2024-02-16 09:00:00', 7, 1);               -- id: 4
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Departamento en Recoleta', 'Departamento luminoso y moderno', 'Av. Callao 1234', -34.5966, -58.3885, 200000.00, 3, true, false, '2024-02-17 10:30:00', 9, 6);                   -- id: 5
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Ático en Miraflores', 'Ático con vista al mar', 'Malecón 28 de Julio 567', -12.1211, -77.0305, 250000.00, 4, true, false, '2024-02-19 12:00:00', 11, 7);                 -- id: 6
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Casa Colonial en Getsemaní', 'Casa con patio interno y piscina', 'Calle de la Sierpe 48', 10.4180, -75.5277, 300000.00, 8, true, false, '2024-02-21 09:45:00', 2, 3);              -- id: 7
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Loft Tech en El Chicó', 'Loft inteligente con domótica', 'Calle 94 #11A-45', 4.6761, -74.0488, 280000.00, 2, true, false, '2024-02-23 08:15:00', 11, 2);                      -- id: 8
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('Casa en Providencia', 'Casa amplia cerca a la playa', 'Av. Providencia 123', -33.4429, -70.6503, 220000.00, 6, true, false, '2024-02-24 11:00:00', 7, 9);                     -- id: 9
INSERT INTO accommodations (title, description, address, lat, lng, price_per_night, max_guests, active, soft_deleted, created_at, host_id, city_id) VALUES ('PH en Polanco', 'Penthouse con terraza y asador', 'Av. Presidente Masaryk 250', 19.4326, -99.2000, 350000.00, 4, true, false, '2024-02-27 13:20:00', 9, 10);              -- id: 10

-- 5) Documentos de usuarios
INSERT INTO user_documents (user_id, document_url) VALUES (2, 'https://docs.example.com/maria_cedula.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (2, 'https://docs.example.com/maria_rut.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (3, 'https://docs.example.com/carlos_cedula.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (7, 'https://docs.example.com/pedro_cedula.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (9, 'https://docs.example.com/javier_rut.pdf');
INSERT INTO user_documents (user_id, document_url) VALUES (11, 'https://docs.example.com/miguel_cedula.pdf');

-- 6) Servicios de alojamientos
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'WiFi');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'Aire');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (1, 'Cocina');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (2, 'Desayuno');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (2, 'Chimenea');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (3, 'Jacuzzi');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (3, 'Gimnasio');
-- Nuevos
INSERT INTO accommodation_services (accommodation_id, service) VALUES (4, 'Parqueadero');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (4, 'BBQ');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (5, 'Ascensor');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (5, 'Lavadora');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (6, 'VistaMar');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (6, 'Balcón');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (7, 'Piscina');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (7, 'Patio');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (8, 'Domótica');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (8, 'Coworking');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (9, 'Playa');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (10, 'Terraza');
INSERT INTO accommodation_services (accommodation_id, service) VALUES (10, 'Asador');

-- 7) Imágenes (alojamientos)
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800', 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=300', 'accommodation_1_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_1_primary', true, '2024-01-16 10:15:00', 1); -- id: 1
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=300', 'accommodation_2_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_2_primary', true, '2024-01-21 11:45:00', 2); -- id: 2
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at, accommodation_id) VALUES ('https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800', 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=300', 'accommodation_3_primary', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_300/accommodation_3_primary', true, '2024-01-26 14:30:00', 3); -- id: 3
-- Perfil de usuarios (sin accommodation_id)
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1494790108755-2616b612b619?w=400', 'https://images.unsplash.com/photo-1494790108755-2616b612b619?w=150', 'user_2_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_2_profile', true, '2024-01-15 10:30:00'); -- id: 4
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=400', 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150', 'user_3_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_3_profile', true, '2024-01-20 15:30:00'); -- id: 5
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400', 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150', 'user_5_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_5_profile', true, '2024-02-05 09:00:00'); -- id: 6
-- Nuevas imágenes de perfil para nuevos usuarios
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150', 'user_7_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_7_profile', true, '2024-02-15 10:35:00'); -- id: 7
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=400', 'https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=150', 'user_8_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_8_profile', true, '2024-02-18 09:50:00'); -- id: 8
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?w=400', 'https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?w=150', 'user_9_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_9_profile', true, '2024-02-20 08:50:00'); -- id: 9
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1544005316-04ce1f2a71c2?w=400', 'https://images.unsplash.com/photo-1544005316-04ce1f2a71c2?w=150', 'user_10_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_10_profile', true, '2024-02-22 13:40:00'); -- id: 10
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1546525848-3ce03ca516f6?w=400', 'https://images.unsplash.com/photo-1546525848-3ce03ca516f6?w=150', 'user_11_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_11_profile', true, '2024-02-25 10:25:00'); -- id: 11
INSERT INTO images (url, thumbnail_url, cloudinary_public_id, cloudinary_thumbnail_url, is_primary, created_at) VALUES ('https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=400', 'https://images.unsplash.com/photo-1531123897727-8f129e1688ce?w=150', 'user_12_profile', 'https://res.cloudinary.com/demo/image/upload/c_thumb,w_150/user_12_profile', true, '2024-02-26 07:45:00'); -- id: 12

-- 7.2) Actualizar usuarios con sus imágenes de perfil
UPDATE users SET profile_image_id = 4 WHERE id = 2;
UPDATE users SET profile_image_id = 5 WHERE id = 3;
UPDATE users SET profile_image_id = 6 WHERE id = 5;
UPDATE users SET profile_image_id = 7 WHERE id = 7;
UPDATE users SET profile_image_id = 8 WHERE id = 8;
UPDATE users SET profile_image_id = 9 WHERE id = 9;
UPDATE users SET profile_image_id = 10 WHERE id = 10;
UPDATE users SET profile_image_id = 11 WHERE id = 11;
UPDATE users SET profile_image_id = 12 WHERE id = 12;

-- 8) Reservaciones (depende de alojamientos y usuarios)
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-01', '2024-03-05', 4, 480000.00, 'COMPLETED', '2024-02-20 10:00:00', 1, 5); -- id: 1
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-10', '2024-03-14', 4, 360000.00, 'COMPLETED', '2024-02-25 14:30:00', 2, 6); -- id: 2
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-01', '2024-04-05', 4, 720000.00, 'CONFIRMED', '2024-03-15 11:20:00', 3, 5); -- id: 3
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, cancelled_at, motivo_cancelacion, cancelled_by, accommodation_id, user_id) VALUES ('2024-05-01', '2024-05-05', 4, 480000.00, 'CANCELLED', '2024-04-15 10:00:00', '2024-04-20 15:30:00', 'Cambio de planes de viaje', 'USER', 1, 6); -- id: 4
-- Nuevas
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-20', '2024-03-23', 3, 450000.00, 'COMPLETED', '2024-03-10 09:00:00', 4, 10); -- id: 5
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-03-25', '2024-03-29', 4, 800000.00, 'COMPLETED', '2024-03-12 12:00:00', 5, 12); -- id: 6
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-10', '2024-04-12', 2, 500000.00, 'COMPLETED', '2024-03-28 10:10:00', 6, 13); -- id: 7
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-15', '2024-04-18', 3, 660000.00, 'CONFIRMED', '2024-04-01 08:30:00', 7, 14); -- id: 8
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-20', '2024-04-24', 4, 1120000.00, 'COMPLETED', '2024-04-03 16:45:00', 8, 5); -- id: 9
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-04-25', '2024-04-28', 3, 660000.00, 'PENDING', '2024-04-05 09:15:00', 9, 6); -- id: 10
INSERT INTO reservations (start_date, end_date, nights, total_price, status, created_at, accommodation_id, user_id) VALUES ('2024-05-02', '2024-05-05', 3, 1050000.00, 'COMPLETED', '2024-04-18 11:20:00', 10, 10); -- id: 11

-- 9) Pagos (depende de reservaciones)
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (480000.00, 'CARD', 'COMPLETED', '2024-02-20 10:15:00', 1);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (360000.00, 'PAYPAL', 'COMPLETED', '2024-02-25 14:45:00', 2);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (450000.00, 'CARD', 'COMPLETED', '2024-03-10 09:10:00', 5);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (800000.00, 'CARD', 'COMPLETED', '2024-03-12 12:10:00', 6);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (500000.00, 'CARD', 'COMPLETED', '2024-03-28 10:20:00', 7);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (1120000.00, 'PAYPAL', 'COMPLETED', '2024-04-03 16:50:00', 9);
INSERT INTO payments (amount, method, status, paid_at, reservation_id) VALUES (1050000.00, 'CARD', 'COMPLETED', '2024-04-18 11:30:00', 11);

-- 10) Comentarios (depende de reservaciones, alojamientos y usuarios)
-- IMPORTANTE: Se incluye host_id (id del usuario anfitrión del alojamiento) para alinear con HostReply.hostId
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (5, 'Excelente apartamento, muy limpio y cómodo. La ubicación es perfecta.', '2024-03-06 15:30:00', false, 2, 'Gracias Laura! Me alegra que hayas disfrutado tu estadía.', '2024-03-06 18:45:00', 1, 1, 5);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (4, 'La casa colonial es hermosa. El único detalle es un poco de ruido.', '2024-03-15 12:20:00', false, 3, 'Gracias Diego, instalaremos nuevas ventanas para mejorar.', '2024-03-15 16:30:00', 2, 2, 6);
-- Nuevos comentarios
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (5, 'La cabaña es un sueño, perfecta para desconectar.', '2024-03-23 10:00:00', false, 7, '¡Gracias! Nos alegra que te haya gustado.', '2024-03-23 12:00:00', 5, 4, 10);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (3, 'Buen departamento pero el ascensor estuvo fuera de servicio 1 día.', '2024-03-29 18:40:00', false, 9, 'Lamentamos el inconveniente, fue un mantenimiento programado.', '2024-03-29 20:10:00', 6, 5, 12);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (4, 'Hermosa vista desde el ático, volvería sin dudar.', '2024-04-12 19:10:00', false, 11, NULL, NULL, 7, 6, 13);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (5, 'El loft tech es perfecto para trabajar remoto.', '2024-04-24 14:30:00', false, 11, 'Gracias por tu comentario, siempre bienvenido.', '2024-04-24 16:00:00', 9, 8, 5);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (4, 'Casa muy cómoda, cercana a la playa.', '2024-04-28 09:00:00', true, 7, NULL, NULL, 10, 9, 6);
INSERT INTO comments (rating, comment_text, created_at, is_moderated, host_id, reply, reply_at, reservation_id, accommodation_id, user_id) VALUES (5, 'PH increíble, la terraza es lo mejor.', '2024-05-05 20:20:00', false, 9, '¡Gracias por elegirnos!', '2024-05-05 21:00:00', 11, 10, 10);

-- 11) Favoritos
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-02-28 14:20:00', 5, 1);
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-03-05 11:15:00', 6, 2);
-- Nuevos
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-03-07 08:00:00', 10, 4);
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-03-18 09:30:00', 12, 5);
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-04-02 17:45:00', 13, 6);
INSERT INTO favorites (created_at, user_id, accommodation_id) VALUES ('2024-04-06 12:10:00', 14, 7);

-- 12) Notificaciones
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Reservación Confirmada', 'Tu reservación en "Apartamento Moderno en El Poblado" fue confirmada.', 'NEW_RESERVATION', '{"reservation_id":1}', false, '2024-02-20 10:20:00', 5);
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Nuevo Comentario', 'Has recibido un nuevo comentario en tu alojamiento.', 'NEW_COMMENT', '{"comment_id":1}', true, '2024-03-06 18:50:00', 2);
-- Nuevas
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Pago Recibido', 'Se registró tu pago correctamente.', 'PAYMENT_RECEIVED', '{"reservation_id":5}', true, '2024-03-10 09:15:00', 10);
INSERT INTO notifications (title, body, type, metadata, is_read, created_at, user_id) VALUES ('Nuevo Favorito', 'Tu alojamiento fue agregado a favoritos.', 'ACCOMMODATION_FAVORITED', '{"accommodation_id":8}', false, '2024-04-24 16:10:00', 11);

-- 13) Tokens de reseteo de contraseña
INSERT INTO password_reset_tokens (token_hash, expires_at, used, created_at, user_id) VALUES ('$2a$10$abcd1234567890abcdef1234567890abcdef123456', '2024-12-15 15:45:00', false, '2024-12-14 15:45:00', 5);
INSERT INTO password_reset_tokens (token_hash, expires_at, used, created_at, user_id) VALUES ('$2a$10$efgh1234567890abcdef1234567890abcdef123456', '2024-12-16 15:45:00', true, '2024-12-15 15:45:00', 6);
-- FIN