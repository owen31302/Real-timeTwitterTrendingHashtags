const cassandra = require('cassandra-driver');

const client = new cassandra.Client({
  // '13.57.254.47' || '127.0.0.1'
  contactPoints: ['127.0.0.1'],
  keyspace: 'twitterkeyspace'
});

module.exports = {
  getLatestData: callback => {
    client.execute('SELECT * FROM twitter LIMIT 1', async (err, result) => {
      if (err) return console.error(err);

      console.log('Retrieve data from cassandra successfully !');
      // console.log(result);

      const row = result.rows[0];
      const year = row.get(0);
      const time = row.get(1);
      const status = row.get(2);

      callback(status);
    });
  }
};

// module.exports = {
//   getLatestData: callback => {
//     client.execute(
//       'SELECT * FROM twitter LIMIT 1',
//       '',
//       { prepare: true },
//       async (err, result) => {
//         if (err) return console.error(err);
//         const row = result.first();
//         callback(row);
//       }
//     );
//   }
// };
