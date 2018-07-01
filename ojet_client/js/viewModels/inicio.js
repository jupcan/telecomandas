define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'ojs/ojarraydataprovider'],
    function(oj, ko, $, app) {

        function IncioViewModel() {
            var self = this;

            getMesas();
            self.mesas=ko.observableArray(mesas);
            self.headerConfig = {'viewName': 'header', 'viewModelFactory': app.getHeaderModel()};
            self.mesas_estados = new oj.ArrayDataProvider(self.mesas, {'idAttribute': 'id'});

            this.renderer = function(context)
            {
                return {'insert':context['data']['id']};
            };

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

        $(
            function()
            {
                //ko.applyBindings(new MesasSoloIdViewModel(), document.getElementById('listViewMesas'));
            }
        );
        return new IncioViewModel();
    }
);