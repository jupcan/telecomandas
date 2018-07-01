define(['ojs/ojcore', 'ojs/ojrouter', 'ojs/ojarraytabledatasource', 'ojs/ojoffcanvas', 'ojs/ojbutton'],
    function(oj) {
        function ControllerViewModel() {
            var self = this;

            // Router setup
            self.router = oj.Router.rootInstance;
            self.router.configure({
                'inicio': {label: 'Inicio', isDefault: true},
                'mesas': {label: 'Mesas'},
                'mesa' : {label:'Mesa'},
                'acercaDe': {label: 'Acerca de'}
            });
            oj.Router.defaults['urlAdapter'] = new oj.Router.urlParamAdapter();
            self.moduleConfig = self.router.moduleConfig;

            // Navigation setup
            var navData = [
                {name: 'Inicio', id: 'inicio',
                    iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-home-icon-24'},
                {name: 'Mesas', id: 'mesas',
                    iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-edit-icon-24'},
                {name: 'Acerca de', id: 'acercaDe',
                    iconClass: 'oj-navigationlist-item-icon demo-icon-font-24 demo-info-icon-24'}
            ];
            self.navDataSource = new oj.ArrayTableDataSource(navData, {idAttribute: 'id'});

            // Drawer setup
            self.toggleDrawer = function() {
                return oj.OffcanvasUtils.toggle({selector: '#navDrawer', modality: 'modal', content: '#pageContent'});
            }
            // Add a close listener so we can move focus back to the toggle button when the drawer closes
            $("#navDrawer").on("ojclose", function() { $('#drawerToggleButton').focus(); });

            // Header Setup
            self.getHeaderModel = function() {
                var headerFactory = {
                    createViewModel: function(params, valueAccessor) {
                        var model =  {
                            pageTitle: self.router.currentState().label,
                            handleBindingsApplied: function(info) {
                                // Adjust content padding after header bindings have been applied
                                self.adjustContentPadding();
                            },
                            toggleDrawer: self.toggleDrawer
                        };
                        return Promise.resolve(model);
                    }
                }
                return headerFactory;
            }

            // Method for adjusting the content area top/bottom paddings to avoid overlap with any fixed regions.
            // This method should be called whenever your fixed region height may change.  The application
            // can also adjust content paddings with css classes if the fixed region height is not changing between
            // views.
            self.adjustContentPadding = function () {
                var topElem = document.getElementsByClassName('oj-applayout-fixed-top')[0];
                var contentElem = document.getElementsByClassName('oj-applayout-content')[0];
                var bottomElem = document.getElementsByClassName('oj-applayout-fixed-bottom')[0];

                if (topElem) {
                    contentElem.style.paddingTop = topElem.offsetHeight+'px';
                }
                if (bottomElem) {
                    contentElem.style.paddingBottom = bottomElem.offsetHeight+'px';
                }
                // Add oj-complete marker class to signal that the content area can be unhidden.
                // See the override.css file to see when the content area is hidden.
                contentElem.classList.add('oj-complete');
            }
        }

        return new ControllerViewModel();
    }
);

var mesas;
var categorias;
var comanda;

function Mesa(id, estado) {
    this.id=id;
    this.estado=estado;
    this.platos=[];
}

Mesa.prototype.addPlato = function(idCategoria, idPlato, nombre) {
    var plato=null;
    for (var i=0; i<this.platos.length; i++) {
        var auxi=this.platos[i];
        if (auxi.idCategoria==idCategoria && auxi.idPlato==idPlato) {
            plato=auxi;
            break;
        }
    }
    if (plato==null) {
        plato=new Plato(idCategoria, idPlato, nombre);
        this.platos.push(plato);
    }
    plato.unidades=plato.unidades+1;
}

function getMesas() {
    var request=new XMLHttpRequest();
    request.open("get", "http://localhost:8080/Comandas/getMesas.jsp", false);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.onreadystatechange = function() {
        if (request.readyState==4 && request.status==200) {
            var respuesta=JSON.parse(request.responseText);
            mesas=[];
            for (var i=0; i<respuesta.length; i++) {
                var mesa=new Mesa(respuesta[i]._id, respuesta[i].estado);
                mesas.push(mesa);
            }
        }
    };
    request.send();
}

function Comanda() {
   this.platosComanda = [];
}

function Plato(idCategoria, idPlato, nombre) {
    this.idCategoria=idCategoria;
    this.idPlato=idPlato;
    this.nombre=nombre;
    this.unidades=0;
}

function Categoria(idCategoria, nombre,platos) {
    this.idCategoria=idCategoria;
    this.nombre=nombre;
    this.platos=platos;
}

function getCategorias() {
    var request=new XMLHttpRequest();
    request.open("get", "http://localhost:8080/Comandas/getCategorias.jsp", false);
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.onreadystatechange = function() {
        if (request.readyState==4 && request.status==200) {
            var respuesta=JSON.parse(request.responseText);
            categorias=[];
            for (var i=0; i<respuesta.length; i++) {
                var categoria = new Categoria(respuesta[i]._id,respuesta[i].nombre, respuesta[i].platos);
                categorias.push(categoria);
            }
        }
    };
    request.send();
}

function recibirComanda(idMesa, platos){
    var request=new XMLHttpRequest();
    request.open("post", "http://localhost:8080/Comandas/recibirComanda.jsp");
    request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    request.onreadystatechange = function() {
        if (request.readyState==4 && request.status==200) {
            var respuesta=JSON.parse(request.responseText);
        }
    };
    var p = {
        id : idMesa,
        platos : platos
    };

    request.send("p=" + JSON.stringify(p));
}