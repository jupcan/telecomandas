define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'ojs/ojarraydataprovider', 'ojs/ojbutton'],
 function(oj, ko, $, app) {

    function MesasViewModel() {
        var self = this;
        getMesas();
        self.headerConfig = {'viewName': 'header', 'viewModelFactory': app.getHeaderModel()};
        self.mesas=ko.observableArray(mesas);
        self.mesas_estados = new oj.ArrayDataProvider(self.mesas, {'idAttribute': 'id'});
        self.mesaActual = ko.observable("1");

        self.irAMesa1 = function() {
            app.router.go("mesa");
        }

      self.abrirMesa = function (event) {
          var idMesa=event.currentTarget.id;
          var request=new XMLHttpRequest();
          request.open("post", "http://localhost:8080/Comandas/abrirMesa.jsp");
          request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
          request.onreadystatechange = function() {
              if (request.readyState==4 && request.status==200) {
                  var respuesta=JSON.parse(request.responseText);
                  getMesas();
                  sessionStorage.idMesa=idMesa;
                  location.reload();
                  //app.router.go("mesa");
              }
          };
          var p = {
              _id : idMesa
          };
          request.send("p=" + JSON.stringify(p));
      }

        self.cerrarMesa = function (event) {
            var idMesa=event.currentTarget.id;
            var request=new XMLHttpRequest();
            request.open("post", "http://localhost:8080/Comandas/cerrarMesa.jsp");
            request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            location.reload();
            var p = {
                _id : idMesa
            };
            request.send("p=" + JSON.stringify(p));

        }

        self.seleccionarMesa = function (event) {
            var idMesa=event.currentTarget.id;
            var request=new XMLHttpRequest();
            request.open("post", "http://localhost:8080/Comandas/seleccionarMesa.jsp");
            request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            request.onreadystatechange = function() {
                if (request.readyState==4 && request.status==200) {
                    var respuesta=JSON.parse(request.responseText);
                    sessionStorage.idMesa=idMesa;
                    app.router.go("mesa");
                }
            };
            var p = {
                _id : idMesa
            };
            request.send("p=" + JSON.stringify(p));
        }
    }
    return new MesasViewModel();
  }
);