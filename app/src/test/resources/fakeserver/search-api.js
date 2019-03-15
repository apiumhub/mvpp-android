const json = require("./response/search.json");

module.exports = {
   path: '/search/repositories',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};