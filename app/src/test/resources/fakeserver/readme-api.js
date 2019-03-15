import * as TestTemplate from './response/readme.html';

module.exports = {
   path: '/repos/:owner/:repository/readme',
   status: (req, res, next) => {
        res.status(200);
        next();
     },
   template: """"""
};