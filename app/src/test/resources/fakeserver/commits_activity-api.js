const json = require("./response/commit_activity.json");

module.exports = {
   path: '/repos/:owner/:repository/stats/commit_activity',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: json
};