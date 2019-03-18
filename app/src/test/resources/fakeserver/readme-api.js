const json = require("./response/branches.json");

module.exports = {
   path: '/repos/:owner/:repository/readme',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};