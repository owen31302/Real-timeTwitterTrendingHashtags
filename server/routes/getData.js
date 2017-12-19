// import { getData } from '../services/cassandra';
const cassandra = require('../services/cassandra');

module.exports = app => {
  app.get('/api/getLatestData', (req, res) => {
    cassandra.getLatestData((status, callback) => {
      // console.log(status);
      var data = [];
      status.forEach(tuple => {
        var obj = new Object();
        obj.state = tuple.get(0);
        obj.tag = tuple.get(1).get(0);
        obj.count = tuple.get(1).get(1);
        data.push(obj);
        // console.log(state + ', ' + tag + ', ' + val);
      });
      res.send(data);
      // res.send(200);
    });
  });
};
