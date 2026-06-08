-- Catálogo de tipos de mercancía (12 tipos v3)
-- Se ejecuta automáticamente en el primer arranque (spring.jpa.hibernate.ddl-auto=update)

INSERT IGNORE INTO tipo_mercancia (id, nombre, nombre_db, descripcion) VALUES
(1,  'Paquetería general',        'paqueteria_general',        'Paquetes, cajas, envíos estándar'),
(2,  'Electrónica frágil',        'electronica_fragil',        'Dispositivos frágiles, equipos electrónicos'),
(3,  'Alimentos frescos',         'alimentos_frescos',         'Productos perecederos (0–8°C), requiere frío'),
(4,  'Alimentos congelados',      'alimentos_congelados',      'Productos congelados (< -18°C), cadena frío estricta'),
(5,  'Materiales de construcción','materiales_construccion',   'Cemento, acero, materiales pesados'),
(6,  'Mobiliario',                'mobiliario',                'Muebles, sofás, piezas grandes'),
(7,  'Mercancía peligrosa (ADR)', 'mercancia_peligrosa',       'Sustancias químicas, inflamables, tóxicas — requiere certificado ADR'),
(8,  'Productos farmacéuticos',   'productos_farmaceuticos',   'Medicinas, biologics — GDP recomendado'),
(9,  'Textil y moda',             'textil_moda',               'Ropa, telas, calzado'),
(10, 'Maquinaria industrial',     'maquinaria_industrial',     'Máquinas, equipos pesados'),
(11, 'Documentación y archivo',   'documentacion_archivo',     'Documentos, libros, archivos'),
(12, 'Vehículos y automoción',    'vehiculos_automocion',      'Coches, motos, piezas vehiculares');
