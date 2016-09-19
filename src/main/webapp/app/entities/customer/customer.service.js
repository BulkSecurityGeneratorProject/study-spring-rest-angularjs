(function() {
    'use strict';
    angular
        .module('restangularjsApp')
        .factory('Customer', Customer);

    Customer.$inject = ['$resource', 'DateUtils'];

    function Customer ($resource, DateUtils) {
        var resourceUrl =  'api/customers/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.initialTerm = DateUtils.convertLocalDateFromServer(data.initialTerm);
                        data.finalTerm = DateUtils.convertLocalDateFromServer(data.finalTerm);
                    }
                    return data;
                }
            },
            'update': {
                method: 'PUT',
                transformRequest: function (data) {
                    data.initialTerm = DateUtils.convertLocalDateToServer(data.initialTerm);
                    data.finalTerm = DateUtils.convertLocalDateToServer(data.finalTerm);
                    return angular.toJson(data);
                }
            },
            'save': {
                method: 'POST',
                transformRequest: function (data) {
                    data.initialTerm = DateUtils.convertLocalDateToServer(data.initialTerm);
                    data.finalTerm = DateUtils.convertLocalDateToServer(data.finalTerm);
                    return angular.toJson(data);
                }
            }
        });
    }
})();
