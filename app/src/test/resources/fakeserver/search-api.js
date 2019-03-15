const json = require("./json/search.json");

module.exports = {
   path: '/search/repositories',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};