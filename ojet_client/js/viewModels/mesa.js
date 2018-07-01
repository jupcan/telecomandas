define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'ojs/ojarraydataprovider', 'ojs/ojbutton' , 'ojs/ojknockout', 'ojs/ojconveyorbelt'],
 function(oj, ko, $, app) {

    function MesaViewModel() {
        var self = this;
        var idCategoria;
        var idMesa=sessionStorage.idMesa;
        self.mesaComanda = idMesa;
        self.mesaActual=ko.observable(idMesa);

        getCategorias();
        self.headerConfig = {'viewName': 'header', 'viewModelFactory': app.getHeaderModel()};
        self.categorias=ko.observableArray(categorias);
        self.categorias_nombre = new oj.ArrayDataProvider(self.categorias, {'idAttribute': 'idCategoria'});

        self.platos=ko.observableArray([]);
        self.platos_nombre = new oj.ArrayDataProvider(self.platos, {'idAttribute': '_id'});
        self.aux=[];
        self.aux1=[];

        getMesas();
        self.mesas=ko.observableArray(mesas);
        self.mesasYPlatos = new oj.ArrayDataProvider(self.mesas, {'idAttribute': 'id'});

        self.comanda=ko.observableArray([]);
        self.comanda_seleccionada=new oj.ArrayDataProvider(self.comanda, {'idAttribute': '_id'});

        self.seleccionarCategoria = function (event) {
            idCategoria = event.currentTarget.id;
            self.aux = buscarPlatosCategoria(idCategoria);
            self.platos(self.aux);
        }

        self.seleccionarPlato = function (event) {
            var idPlato = event.currentTarget.id;
            var nombre;
            for(var j = 0; j<self.platos().length; j++){
                if(idPlato == self.platos()[j]._id){
                    nombre = self.platos()[j].nombre;
                }
            }
            for(var j = 0; j<self.mesas().length; j++){
                if(self.mesas()[j].id == idMesa){
                    self.mesas()[j].addPlato(idCategoria,idPlato, nombre);
                    mostrarComanda(self.mesas()[j], self);
                }
            }
        }

        self.confirmarComanda = function (event) {
            console.log(self.aux1);
            recibirComanda(self.mesaComanda, self.aux1);
            for(var j = 0; j<self.mesas().length; j++){
                if(self.mesas()[j].id == idMesa){
                    self.mesas()[j].platos = [];
                }
            }
        }

        self.handleActivated = function(info) {
            // Implement if needed
        };

        self.handleAttached = function(info) {
            // Implement if needed
        };

        self.handleBindingsApplied = function(info) {
            // Implement if needed
        };

        self.handleDetached = function(info) {
            // Implement if needed
        };
    }

    function buscarPlatosCategoria(idCategoria) {
        var platos = [];
        for(var i = 0; i<categorias.length; i++){
            if(categorias[i].idCategoria  == idCategoria){
                for(var j = 0; j<categorias[i].platos.length; j++) {
                    platos.push(categorias[i].platos[j]);
                }
            }
        }
        return platos;
    }

    function mostrarComanda(mesa, self){
        self.aux1 = mesa.platos;
        self.comanda(self.aux1);
    }
    return new MesaViewModel();
  }
);