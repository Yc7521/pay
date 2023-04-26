

INSERT INTO pay.user_info (id, credible, deleted, money, nickname, user_type)
VALUES (1, true, false, 1000.0000, 'admin', 'Admin'),
       (2, true, false, 1000.0000, '1', 'Business'),
       (3, true, false, 1000.0000, '2', 'Business');

INSERT INTO pay.user (id, password, username, user_info_id)
VALUES (1, '$2a$10$nYqBafr3DkZo83/iIh61HO9c76a.oCK3/dTfR0Z.b5FmYkGPw5Rxa', 'admin', 1),
       (2, '$2a$10$gDP.EjSywJ9TcfDeeZjSJuhYTBClcVr8/bOs5g.xtTLB0Qe/VKSe.', '1', 2),
       (3, '$2a$10$HAVmBxG13tk8NSeQIJoAd.FXBqtKSPDSNMgcBZffufnsJSaTB1uou', '2', 3);
