const json = require("./json/branches.json");

module.exports = {
   path: '/repos/:owner/:repository/branches',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};