const json = require("./json/readme.json");

module.exports = {
   path: '/repos/:owner/:repository/readme',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};