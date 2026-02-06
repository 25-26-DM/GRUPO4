package com.example.electronicazytron.model.repository

import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.model.entities.ProductoDao

class ProductoRepository(private val productoDao: ProductoDao) {

    suspend fun agregar(producto: Producto) {
        // Nuevo producto: marcar como no sincronizado para que el servicio lo suba
        producto.isSynced = false
        productoDao.insertar(producto)
    }

    suspend fun listar(): List<Producto> {
        return productoDao.listar()
    }

    suspend fun find(codigo: String): Producto? {
        return productoDao.buscarPorCodigo(codigo)
    }

    suspend fun update(producto: Producto) {
        // Cualquier cambio local debe marcarse como no sincronizado
        producto.isSynced = false
        productoDao.actualizar(producto)
    }

    suspend fun softDelete(codigo: String) {
        // Marcamos como eliminado y como no sincronizado para que el servicio lo elimine en la nube
        productoDao.marcarEliminadoNoSincronizado(codigo)
    }

    suspend fun count(): Int = productoDao.count()

    suspend fun seedIfEmpty(){
        if(productoDao.count() > 0) return

        val seed = listOf(
            Producto("P001","Laptop Lenovo IdeaPad 3","2024-01-15",750.0,10,"https://picsum.photos/seed/P001/400/400", eliminado = false),
            Producto("P002","Mouse inalámbrico Logitech M185","2023-11-20",18.5,45,"https://picsum.photos/seed/P002/400/400", eliminado = false),
            Producto("P003","Teclado mecánico Redragon Kumara","2024-02-10",65.99,25,"https://picsum.photos/seed/P003/400/400", eliminado = false),
            Producto("P004","Monitor Samsung 24 FHD","2023-12-05",180.0,12,"https://picsum.photos/seed/P004/400/400", eliminado = false),
            Producto("P005","Disco SSD Kingston 1TB","2024-03-01",95.75,30,"https://picsum.photos/seed/P005/400/400", eliminado = false),
            Producto("P006","Audífonos Bluetooth JBL Tune 510BT","2024-01-28",48.9,20,"https://picsum.photos/seed/P006/400/400", eliminado = false)
        )

        seed.forEach { productoDao.insertar(it) }

    }
}
