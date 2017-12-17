// import { getData } from '../services/cassandra';
const cassandra = require('../services/cassandra');

cassandra();

module.exports = app => {
  app.get('/', (req, res) => {
    res.send('jdaoif');
  });
};
