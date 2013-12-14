import sys
import urllib2
import json
from collections import defaultdict
from collections import Counter
import numpy

def main(argv):
	urls = argv[0:]
	counters = Counter()
	meters = defaultdict(Counter)
	timers = defaultdict(Counter)

	for url in urls:
		f = urllib2.urlopen(url)
		metric = json.loads(f.read())
		
		# merge counters
		for counter in metric['counters'].keys():
			# see if counter already exists
			# print counter
			counters[counter] += metric['counters'][counter]['count']
			
		# merge meters
		for meter in metric['meters'].keys():
			# print meter
			meters[meter]['count'] += metric['meters'][meter]['count']

		# merge timers
		for timer, values in metric['timers'].items():
			timers[timer]['count'] += metric['timers'][timer]['count']
			timers[timer]['max'] = max(timers[timer]['max'], metric['timers'][timer]['max'])
			
			if timers[timer]['min'] == 0:
				timers[timer]['min'] = metric['timers'][timer]['min']
			else:
				timers[timer]['min'] = min(timers[timer]['min'], metric['timers'][timer]['min'])

			if timers[timer]['means'] == 0:
				timers[timer]['means'] = []
				timers[timer]['p50s'] = []
				timers[timer]['p75s'] = []
				timers[timer]['p95s'] = []
				timers[timer]['p98s'] = []
				timers[timer]['p99s'] = []
				timers[timer]['p999s'] = []
				timers[timer]['weight'] = []

			timers[timer]['means'].append(metric['timers'][timer]['mean'])
			timers[timer]['p50s'].append(metric['timers'][timer]['p50'])
			timers[timer]['p75s'].append(metric['timers'][timer]['p75'])
			timers[timer]['p95s'].append(metric['timers'][timer]['p95'])
			timers[timer]['p98s'].append(metric['timers'][timer]['p98'])
			timers[timer]['p99s'].append(metric['timers'][timer]['p99'])
			timers[timer]['p999s'].append(metric['timers'][timer]['p999'])
			timers[timer]['weight'].append(metric['timers'][timer]['count'])
			timers[timer]['duration_units'] = metric['timers'][timer]['duration_units']
			
	# average timers
	for name, timer in timers.items():
		timer['mean'] = numpy.average(timer['means'], weights=timer['weight'])
		timer['p50'] = numpy.average(timer['p50s'], weights=timer['weight'])
		timer['p75'] = numpy.average(timer['p75s'], weights=timer['weight'])
		timer['p95'] = numpy.average(timer['p95s'], weights=timer['weight'])
		timer['p98'] = numpy.average(timer['p98s'], weights=timer['weight'])
		timer['p99'] = numpy.average(timer['p99s'], weights=timer['weight'])
		timer['p999'] = numpy.average(timer['p999s'], weights=timer['weight'])
		del timer['means']
		del timer['p50s']
		del timer['p75s']
		del timer['p95s']
		del timer['p98s']
		del timer['p99s']
		del timer['p999s']
		del timer['weight']
			
	print '=' * 80
	print 'COUNTERS'
	for k, v in counters.items():
		print '  {0}: {1}'.format(k, v)
	print

	print '=' * 80
	print 'METERS'
	for key, counter in sorted(meters.items()):
		print key
		for k, v in counter.items():
			print '  {0}: {1}'.format(k, v)
	print

	print '=' * 80
	print 'TIMERS'
	for key, counter in sorted(timers.items()):
		print key
		for k, v in sorted(counter.items()):
			print '  {0}: {1}'.format(k, v)
			
if __name__ == "__main__":
	main(sys.argv[1:])
