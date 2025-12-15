package com.example.electronicazytron.modelo
//clase producto usada para implementar las diferentes acciones que puede realizar el objeto
//en este caso esta la inicializaicion de la lista de objetos y el crud del objeto
class ProductoRepository {
//usando mutableListOf se puede crear listas dinamicas las cuales pueden ser modificadas y almacenar el estado de la lista
    // optimizando la vista en tiempo real de la lista, evita que se tenga que crear metodos auxiliares para refrescar pantallas que contengan la lista implementada
    private val productos = mutableListOf(
        Producto(
            codigo = "P001",
            descripcion = "Laptop Lenovo IdeaPad 3",
            fecha_fab = "2024-01-15",
            costo = 750.00,
            disponibilidad = 10
        ),
        Producto(
            codigo = "P002",
            descripcion = "Mouse inalámbrico Logitech M185",
            fecha_fab = "2023-11-20",
            costo = 18.50,
            disponibilidad = 45
        ),
        Producto(
            codigo = "P003",
            descripcion = "Teclado mecánico Redragon Kumara",
            fecha_fab = "2024-02-10",
            costo = 65.99,
            disponibilidad = 25
        ),
        Producto(
            codigo = "P004",
            descripcion = "Monitor Samsung 24\" FHD",
            fecha_fab = "2023-12-05",
            costo = 180.00,
            disponibilidad = 12
        ),
        Producto(
            codigo = "P005",
            descripcion = "Disco SSD Kingston 1TB",
            fecha_fab = "2024-03-01",
            costo = 95.75,
            disponibilidad = 30
        ),
        Producto(
            codigo = "P006",
            descripcion = "Audífonos Bluetooth JBL Tune 510BT",
            fecha_fab = "2024-01-28",
            costo = 48.90,
            disponibilidad = 20
        )
    )
    //funcion para agregar nuevos productos
    fun agregar(producto: Producto){
        productos.add(producto)
    }
    //funcion para listar los productos
    fun listar(): List<Producto>{
        return  productos;
    }
    //funcion para buscar el producto, se usa el metodo find seguido de it el cual recorre la lista de productos y el atributo a buscar
    fun find(codigo: String): Producto?{
        return productos.find{it.codigo==codigo}
    }
    //metodo para modificar una lista, como estan haciendo referencia a la misma lista es posible unicamente modificar el objeto encontrado
    //y se actualiza automaticamente en la lista generada
    fun update(codigo: String,producto: Producto){
        var obj = find(codigo)
        if (obj!=null){
            obj.costo = producto.costo
            obj.disponibilidad=producto.disponibilidad
            obj.descripcion=producto.descripcion
            obj.fecha_fab=producto.fecha_fab
        }
    }
    //metodo para eliminar un elemento de la lista
    fun delete(codigo: String) {
        val producto = productos.find { it.codigo == codigo}
        if (producto!=null){
            productos.remove(producto)
        }
    }
    //aqui se puede agregar los metodos que un producto puede hacer, o mas extenciones del crud del producto
}