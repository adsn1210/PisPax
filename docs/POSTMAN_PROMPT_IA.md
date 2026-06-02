Crea una coleccion de Postman para una API REST llamada PisPax con base URL http://localhost:8080.

La autenticacion es JWT Bearer Token. El token se obtiene haciendo POST a /api/auth/login y se debe guardar en una variable de entorno llamada "token". En los endpoints protegidos usar {{token}} como Bearer Token.

Endpoints:

POST /api/auth/registro - publica - body JSON: { "nombre": string, "apellidos": string, "email": string, "password": string (min 8), "telefono": string opcional, "rol": "CLIENTE" | "TRANSPORTISTA" }

POST /api/auth/login - publica - body JSON: { "email": string, "password": string } - guardar el campo "token" de la respuesta en la variable de entorno "token"

GET /api/tipo-mercancia - publica - sin body

POST /api/viajes - requiere JWT de rol CLIENTE - body JSON: { "tipoMercanciaId": number, "descripcionMercancia": string opcional, "direccionRecogida": string, "direccionEntrega": string, "pesoKg": number positivo }

GET /api/viajes - requiere JWT - sin body - devuelve los viajes del usuario autenticado

GET /api/viajes/disponibles - requiere JWT de rol TRANSPORTISTA - sin body - devuelve viajes en estado PENDIENTE

GET /api/viajes/{id} - requiere JWT - sin body

PUT /api/viajes/{id}/aceptar - requiere JWT de rol TRANSPORTISTA - body JSON: { "vehiculoId": number }

PUT /api/viajes/{id}/estado - requiere JWT de rol TRANSPORTISTA - body JSON: { "nuevoEstado": "SALIDA_RECOGIDA" | "LLEGADA_RECOGIDA" | "MERCANCIA_RECOGIDA" | "SALIDA_ENTREGA" | "LLEGADA_ENTREGA" | "COMPLETADO" | "CANCELADO" }

POST /api/vehiculos - requiere JWT de rol TRANSPORTISTA - body JSON: { "matricula": string, "marca": string, "modelo": string, "tipoVehiculo": "FURGONETA" | "FURGON_GRANDE" | "FRIGORIFICO" | "CAMION_LIGERO" | "CAMION_PESADO" | "CAMION_ARTICULADO" | "PLATAFORMA", "subtipo": string opcional, "taraKg": number opcional, "capacidadKg": number positivo, "mmaKg": number opcional, "carnetRequerido": "B" | "C1" | "C" | "C_E" }

GET /api/vehiculos/mis-vehiculos - requiere JWT de rol TRANSPORTISTA - sin body

Organiza los endpoints en carpetas: Auth, Viajes, Vehiculos, Catalogo. Añade ejemplos de body con datos de prueba realistas.
