const json = require("./response/repositories.json");

module.exports = {
   path: '/repositories',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};