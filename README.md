Code for my bachelor's thesis on mobile application framework for diagnosis of skin cancer.

This repository will be updated with more information on code structure.

## Abstract

Skin cancer is the one most common forms of cancer among fair-skinned individuals.
It occurs on various depths of the skin, and exists in three forms Squamous Cell Car-
cinoma (SCC), Basal Cell Carcinoma (BCC) and Malignant Melanoma (MM). While
the first two are relatively harmless, Melanoma is a deadly form of cancer where the
cells producing protective melanin mutate and metastasize quickly to surrounding tis-
sue. Early diagnosis is essential for successful treatment. It is, however difficult for
non-experts to diagnose and referrals to expert dermatologists are puncutated by long
waiting times due to the relative shortage of expert dermatologists. Due to the highly
visible nature of melanoma, computer vision and image processing techniques can be
used to produce a preliminary diagnosis. This paper lays foundational work on a mobile
application framework for automated diagnosis of skin cancer. We develop a prototype
mobile application, develop strategies for integration of the application and support
systems and explore algorithms for preprocessing and extraction of lesions from der-
moscopic images.

# Server

The server code is situated inside the `server/` directory. The easiest way to configure it is
using virtualenv. It depends on the python programming language, the other dependencies should
be resolved using `pip`. A nice guide can be found here: http://docs.python-guide.org/en/latest/dev/virtualenvs/

After installation (and entering the virtualenv), you can simply run "python server.py" and it
should run the server.

You must compile the image processor located in `server/services/backend/` by running `make` in
that directory. It is a simple (unoptimised) image processor that takes an image and tries to 
gather metrics required for a dermoscopic diagnosis. At the current stage, nothing is done to these
metrics. It is a very slow process so it is not yet ready-for-use yet --- especially considering
that the server blocks while the image is processed (thereby blocking the client) --- and exists
purely as a proof-of-concept. There are many opportunities for optimisation such as using Opencv's
`gpu` module and removing some of the intermediary steps as the code is written with readability
and illustration in mind rather than speed.

Again, *DO NOT* use any of these for production since it is very fragile.

# Client

Information on the client will go here once the code is cleaned up ...
